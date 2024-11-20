package com.packt;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class SlashCommandListener extends ListenerAdapter {
    private static final String TARGET_CATEGORY_ID = "1303621015611379794";
    private static final String SUPPORT_CHANNEL_ID = "1302004013117739008";
    private static final String WELCOME_MESSAGE_PATH = "src/main/resources/welcomeMessage.txt";
    private static final String BETTING_CHANNEL_ID = "1308746477534707772";
    private static final String ROLES_PATH = "src/main/resources/roles.txt";
    private final ReentrantReadWriteLock welcomeLock = new ReentrantReadWriteLock(true);
    private final ReentrantReadWriteLock roleLock = new ReentrantReadWriteLock(true);
    private final Lock welcomeReadLock = welcomeLock.readLock();
    private final Lock welcomeWriteLock = welcomeLock.writeLock();
    private final Lock roleReadLock = roleLock.readLock();
    private final Lock roleWriteLock = roleLock.writeLock();

    private record Game(String playerA, double scoreA, String playerB, double scoreB) {
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        switch (event.getName()){
            case "Create betting polls" -> createPollsHandler(event);
            case "Close poll" -> closePollHandler(event);
        }
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "create-channel" -> createChannelHandler(event);
            case "show-roles" -> showRolesHandler(event);
            case "add-role" -> addRoleHandler(event);
            case "remove-role" -> removeRoleHandler(event);
            case "show-welcome-message" -> showWelcomeMessageHandler(event);
            case "set-welcome-message" -> setWelcomeMessageHandler(event);
            default -> event.reply("Unknown command!").setEphemeral(true).queue();
        }
    }

    private synchronized void closePollHandler(@NotNull MessageContextInteractionEvent event){
        event.getTarget().endPoll().queue();
        event.reply("Successfully closed poll!").setEphemeral(true).queue();
    }

    private synchronized void createPollsHandler(@NotNull MessageContextInteractionEvent event) {
        String bettingMsg = event.getTarget().getContentRaw();
        String[] lines = bettingMsg.split("\\R");
        List<Game> games;
        try {
            games = getGamesList(lines);
        } catch (NumberFormatException e) {
            event.reply("Wrong message format! Correct format is: PlayerA PlayerA_rate PlayerB PlayerB_rate." +
                            " Rates should be written using dot e.g. 0.1 or as whole numbers e.g. 2")
                    .setEphemeral(true).queue();
            return;
        }
        Guild guild = event.getGuild();
        if (guild == null) {
            return;
        }
        TextChannel channel = guild.getTextChannelById(BETTING_CHANNEL_ID);
        if (channel == null) {
            return;
        }
        games.forEach(game -> channel.sendMessagePoll(
                        MessagePollData.builder("%s vs %s".formatted(game.playerA, game.playerB))
                                .addAnswer("%s %.02f".formatted(game.playerA, game.scoreA))
                                .addAnswer("%s %.02f".formatted(game.playerB, game.scoreB))
                                .setDuration(Duration.ofDays(7))
                                .build()
                ).queue()
        );
        event.reply("Successfully created polls!").setEphemeral(true).queue();
    }

    private List<Game> getGamesList(String... lines) throws NumberFormatException {
        List<Game> games;
        System.out.println(lines[0]);
        System.out.println("=".repeat(40));
        System.out.println(Arrays.toString(lines[0].split(" ")));
        games = Arrays.stream(lines)
                .map(String::trim)
                .map(line -> line.split(" "))
                .map(split -> new Game(split[0], Double.parseDouble(split[1]),
                        split[2], Double.parseDouble(split[3])))
                .toList();

        return games;
    }

    private synchronized void createChannelHandler(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        Member playerA = Objects.requireNonNull(event.getOption("first-player")).getAsMember();
        Member playerB = Objects.requireNonNull(event.getOption("second-player")).getAsMember();
        if(playerA == null || playerB == null) {
            return;
        }
        var matchChannel = createMatchChannel(guild, playerA, playerB);
        if (matchChannel == null) {
            event.reply("Match channel already exists!").setEphemeral(true).queue();
            return;
        }

        editChannel(matchChannel, guild, playerA, playerB).queue(
                (TextChannel channel) -> {
                    event.reply("Text channel `" + channel.getName() + "` has been created!")
                            .setEphemeral(true)
                            .queue();
                    channel.sendMessage(getWelcomeMessage(guild, playerA, playerB)).queue();
                },
                (Throwable error) -> event.reply("Failed to create the channel.")
                        .setEphemeral(true)
                        .queue()
        );
    }

    private void showWelcomeMessageHandler(SlashCommandInteractionEvent event) {
        String welcomeMessage;
        welcomeReadLock.lock();
        try {
            welcomeMessage = readWelcomeMessage();
        } finally {
            welcomeReadLock.unlock();
        }
        if (welcomeMessage == null) {
            event.reply("Welcome message is not set!")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        event.reply("Welcome message is: " + welcomeMessage)
                .setEphemeral(true)
                .queue();
    }

    private void setWelcomeMessageHandler(SlashCommandInteractionEvent event) {
        String message = Objects.requireNonNull(event.getOption("message")).getAsString();
        welcomeWriteLock.lock();
        try {
            writeToFile(message, Path.of(WELCOME_MESSAGE_PATH));
        } finally {
            welcomeWriteLock.unlock();
        }
        event.reply("Welcome message has been set successfully")
                .setEphemeral(true)
                .queue();
    }

    private void writeToFile(String message, Path path) {
        try (var writer = Files.newBufferedWriter(path)) {
            writer.write(message);
        } catch (IOException e) {
            System.out.println("Error occurred while trying to write to welcomeMessage.txt: " + e.getMessage());
        }
    }

    private void showRolesHandler(SlashCommandInteractionEvent event) {
        List<Role> roleList;
        roleReadLock.lock();
        try {
            roleList = readRoles(Objects.requireNonNull(event.getGuild()));
        } finally {
            roleReadLock.unlock();
        }
        if (roleList == null || roleList.isEmpty()) {
            event.reply("No roles set").setEphemeral(true).queue();
            return;
        }
        String roles = roleList.stream().map(IMentionable::getAsMention).collect(Collectors.joining(", ", "", ""));
        event.reply(roles).setEphemeral(true).queue();
    }

    private void addRoleHandler(SlashCommandInteractionEvent event) {
        List<Role> roleList;
        roleReadLock.lock();
        try {
            roleList = readRoles(Objects.requireNonNull(event.getGuild()));
        } finally {
            roleReadLock.unlock();
        }
        Role newRole = Objects.requireNonNull(event.getOption("role")).getAsRole();
        if (roleList.contains(newRole)) {
            event.reply("Role is already set").setEphemeral(true).queue();
            return;
        }
        roleList.add(newRole);
        roleWriteLock.lock();
        try {
            writeToFile(roleList.stream().map(Role::getName).collect(Collectors.joining(System.lineSeparator())),
                    Path.of(ROLES_PATH));
        } finally {
            roleWriteLock.unlock();
        }
        event.reply("Added " + newRole.getAsMention() + " successfully").setEphemeral(true).queue();
    }

    private void removeRoleHandler(SlashCommandInteractionEvent event) {
        List<Role> roleList;
        roleReadLock.lock();
        try {
            roleList = readRoles(Objects.requireNonNull(event.getGuild()));
        } finally {
            roleReadLock.unlock();
        }
        Role roleToRemove = Objects.requireNonNull(event.getOption("role")).getAsRole();
        roleList.remove(roleToRemove);
        roleWriteLock.lock();
        try {
            writeToFile(roleList.stream().map(Role::getName).collect(Collectors.joining(System.lineSeparator())),
                    Path.of(ROLES_PATH));
        } finally {
            roleWriteLock.unlock();
        }
        event.reply("Removed " + roleToRemove.getAsMention() + " successfully").setEphemeral(true).queue();
    }

    private ChannelAction<TextChannel> createMatchChannel(Guild guild, Member playerA, Member playerB) {
        System.out.println(guild.getCategories());
        Category category = guild.getCategoryById(TARGET_CATEGORY_ID);
        String playerAName = playerA.getEffectiveName().toLowerCase();
        String playerBName = playerB.getEffectiveName().toLowerCase();
        if (matchChannelExists(playerAName, playerBName, category)) {
            return null;
        }
        return guild.createTextChannel("%s-%s".formatted(playerAName, playerBName), category);
    }

    private ChannelAction<TextChannel> editChannel(ChannelAction<TextChannel> action, Guild guild, Member playerA, Member playerB) {
        Member guildOwner = guild.getOwner();
        action = hideChannel(action, guild);

        List<Role> roleList;
        roleReadLock.lock();
        try {
            roleList = readRoles(guild);
        } finally {
            roleReadLock.unlock();
        }
        if (roleList != null && !roleList.isEmpty()) {
            for (Role role : roleList) {
                action = addRoleToChannel(action, role);
            }
        }
        action = addMemberToChannel(action, guildOwner);
        action = addMemberToChannel(action, playerA);
        action = addMemberToChannel(action, playerB);
        action = addMemberToChannel(action, guild.getSelfMember());
        return action;
    }

    private List<Role> readRoles(Guild guild) {
        try (Scanner scanner = new Scanner(Path.of(ROLES_PATH))) {
            scanner.useDelimiter(System.lineSeparator());
            List<String> rolesNames = scanner.tokens().toList();
            if (rolesNames.isEmpty()) {
                return new ArrayList<>();
            }
            var tokens = rolesNames.stream()
                    .map(roleName -> guild.getRolesByName(roleName.trim(), true).getFirst())
                    .collect(Collectors.toList());
            scanner.reset();
            return tokens;
        } catch (IOException e) {
            System.out.println("Error occurred while reading roles.txt: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    private String getWelcomeMessage(Guild guild, Member playerA, Member playerB) {
        String message;
        welcomeReadLock.lock();
        try {
            message = readWelcomeMessage();
        } finally {
            welcomeReadLock.unlock();
        }
        if (message == null || message.isEmpty()) {
            return "No welcome message set";
        }
        var supportChannels = guild.getTextChannelById(SUPPORT_CHANNEL_ID);
        message = message.replaceAll("\\{support}",
                supportChannels == null ? "Support" : supportChannels.getAsMention());
        return fillPlayersInMessage(message, playerA, playerB);
    }

    private String readWelcomeMessage() {
        try {
            return new String(Files.readAllBytes(Path.of(WELCOME_MESSAGE_PATH)));
        } catch (IOException e) {
            System.out.println("Error occurred while trying to read welcome message: " + e.getMessage());
        }
        return null;
    }

    private String fillPlayersInMessage(String message, Member playerA, Member playerB) {
        message = message.replaceAll("\\{playerA}", playerA.getAsMention());
        message = message.replaceAll("\\{playerB}", playerB.getAsMention());
        return message;
    }

    private ChannelAction<TextChannel> hideChannel(ChannelAction<TextChannel> action, Guild guild) {
        return action.addPermissionOverride(guild.getPublicRole(), null, EnumSet.of(Permission.VIEW_CHANNEL));
    }

    private ChannelAction<TextChannel> addMemberToChannel(ChannelAction<TextChannel> action, Member member) {
        return action.addPermissionOverride(member, EnumSet.of(Permission.VIEW_CHANNEL), null);
    }

    private ChannelAction<TextChannel> addRoleToChannel(ChannelAction<TextChannel> action, Role role) {
        return action.addPermissionOverride(role, EnumSet.of(Permission.VIEW_CHANNEL), null);
    }

    private boolean matchChannelExists(String playerAName, String playerBName, Category category) {
        return channelExists("%s-%s".formatted(playerAName, playerBName), category) ||
                channelExists("%s-%s".formatted(playerBName, playerAName), category);
    }

    private boolean channelExists(String channelName, Category category) {
        for (TextChannel textChannel : category.getTextChannels()) {
            if (textChannel.getName().equals(channelName)) {
                return true;
            }
        }
        return false;
    }
}