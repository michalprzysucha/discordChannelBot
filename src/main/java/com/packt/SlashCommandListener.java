package com.packt;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class SlashCommandListener extends ListenerAdapter {
    private static final String targetCategoryName = "mecze";

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

    private void createChannelHandler(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        Member playerA = Objects.requireNonNull(event.getOption("first-player")).getAsMember();
        Member playerB = Objects.requireNonNull(event.getOption("second-player")).getAsMember();
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
                    channel.sendMessage(getWelcomeMessage(playerA, playerB)).queue();
                },
                (Throwable error) -> event.reply("Failed to create the channel.")
                        .setEphemeral(true)
                        .queue()
        );
    }

    private void showWelcomeMessageHandler(SlashCommandInteractionEvent event) {
        String welcomeMessage = readWelcomeMessage();
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
        writeToFile(message, Path.of("src/main/resources/welcomeMessage.txt"));
        event.reply("Welcome message has been set successfully")
                .setEphemeral(true)
                .queue();
    }

    private void writeToFile(String message, Path path){
        try(var writer = Files.newBufferedWriter(path)){
            writer.write(message);
        } catch (IOException e) {
            System.out.println("Error occurred while trying to write to welcomeMessage.txt: " + e.getMessage());
        }
    }

    private void showRolesHandler(SlashCommandInteractionEvent event) {
        List<Role> roleList = readRoles(Objects.requireNonNull(event.getGuild()));
        if (roleList == null || roleList.isEmpty()) {
            event.reply("No roles set").setEphemeral(true).queue();
            return;
        }
        String roles = roleList.stream().map(IMentionable::getAsMention).collect(Collectors.joining(", ", "", ""));
        event.reply(roles).setEphemeral(true).queue();
    }

    private void addRoleHandler(SlashCommandInteractionEvent event) {
        List<Role> roleList = readRoles(event.getGuild());
        Role newRole = Objects.requireNonNull(event.getOption("role")).getAsRole();
        if (roleList.contains(newRole)) {
            event.reply("Role is already set").setEphemeral(true).queue();
            return;
        }
        roleList.add(newRole);
        writeToFile(roleList.stream().map(Role::getName).collect(Collectors.joining(System.lineSeparator())),
                Path.of("src/main/resources/roles.txt"));
        event.reply("Added " + newRole.getAsMention() + " successfully").setEphemeral(true).queue();
    }

    private void removeRoleHandler(SlashCommandInteractionEvent event) {
        var roleList = readRoles(event.getGuild());
        Role roleToRemove = Objects.requireNonNull(event.getOption("role")).getAsRole();
        roleList.remove(roleToRemove);
        writeToFile(roleList.stream().map(Role::getName).collect(Collectors.joining(System.lineSeparator())),
                Path.of("src/main/resources/roles.txt"));
        event.reply("Removed " + roleToRemove.getAsMention() + " successfully").setEphemeral(true).queue();
    }

    private ChannelAction<TextChannel> createMatchChannel(Guild guild, Member playerA, Member playerB) {
        Category category = guild.getCategoriesByName(targetCategoryName, true).getFirst();
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

        List<Role> roleList = readRoles(guild);
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
        try (Scanner scanner = new Scanner(Path.of("src/main/resources/roles.txt"))) {
            scanner.useDelimiter(System.lineSeparator());
            List<String> rolesNames = scanner.tokens().toList();
            if(rolesNames.isEmpty()) {
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

    private String getWelcomeMessage(Member playerA, Member playerB) {
        String message = readWelcomeMessage();
        if (message == null || message.isEmpty()) {
            return "No welcome message set";
        }
        return fillPlayersInMessage(message, playerA, playerB);
    }

    private String readWelcomeMessage() {
        try {
            return new String(Files.readAllBytes(Path.of("src/main/resources/welcomeMessage.txt")));
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