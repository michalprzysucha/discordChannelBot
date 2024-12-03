package com.packt.controllers;

import com.packt.services.BettingService;
import com.packt.services.ConfigService;
import com.packt.services.TextChannelService;
import com.packt.services.impl.BettingServiceImpl;
import com.packt.services.impl.ConfigServiceImpl;
import com.packt.services.impl.TextChannelServiceImpl;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class CommandListener extends ListenerAdapter {
    private final BettingService bettingService = new BettingServiceImpl();
    private final TextChannelService textChannelService = new TextChannelServiceImpl();
    private final ConfigService configService = new ConfigServiceImpl();

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Guild guild = event.getGuild();
        configService.createGuildConfigFile(Path.of(guild.getName() + ".json"));
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        switch (event.getName()){
            case "Create betting polls" -> createPollHandler(event);
            case "Close poll" -> closePollHandler(event);
            case "Set welcome message" -> setWelcomeMessageHandler(event);
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
            case "set-games-category" -> setGamesCategoryHandler(event);
            case "show-games-category" -> showGamesCategoryHandler(event);
            case "set-betting-channel" -> setBettingChannelHandler(event);
            case "show-betting-channel" -> showBettingChannelHandler(event);
            default -> event.reply("Unknown command!").setEphemeral(true).queue();
        }
    }

    private synchronized void closePollHandler(@NotNull MessageContextInteractionEvent event){
        bettingService.closePoll(event);
        event.reply("Successfully closed poll!").setEphemeral(true).queue();
    }

    private synchronized void createPollHandler(@NotNull MessageContextInteractionEvent event) {
        try{
            boolean flag = bettingService.createPoll(event);
            if(flag) {
                event.reply("Successfully created polls!").setEphemeral(true).queue();
            }
            else {
                event.reply("Failed to create poll! Check if betting channel is set").setEphemeral(true).queue();
            }
        }
        catch (NumberFormatException e) {
            event.reply("Wrong message format! Correct format is: PlayerA PlayerA_rate PlayerB PlayerB_rate." +
                            " Rates should be written using dot e.g. 0.1 or as whole numbers e.g. 2")
                    .setEphemeral(true).queue();
        }
    }

    private synchronized void createChannelHandler(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if (guild == null) {
            event.reply("This command can only be used in a server.").setEphemeral(true).queue();
            return;
        }
        Member playerA = Objects.requireNonNull(event.getOption("first-player")).getAsMember();
        Member playerB = Objects.requireNonNull(event.getOption("second-player")).getAsMember();
        var textChannel = textChannelService.createChannel(guild, playerA, playerB);
        if(textChannel == null){
            event.reply("Failed to create channel! Channel already exists or no category was set")
                    .setEphemeral(true).queue();
            return;
        }
        textChannelService.editChannel(textChannel, guild, playerA, playerB).queue(
                (TextChannel channel) -> {
                    event.reply("Text channel `" + channel.getName() + "` has been created!")
                            .setEphemeral(true)
                            .queue();
                    channel.sendMessage(fillPlayersPlaceholders(
                            configService.getWelcomeMessage(Path.of(guild.getName() + ".json")),
                                    playerA, playerB))
                            .queue();
                },
                (Throwable error) -> event.reply("Failed to create the channel.")
                        .setEphemeral(true)
                        .queue()
        );
    }

    private void showWelcomeMessageHandler(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if(guild == null){
            return;
        }
        Path jsonConfig = Path.of(guild.getName() + ".json");
        String message = configService.getWelcomeMessage(jsonConfig);
        if (message == null) {
            event.reply("Welcome message is not set!")
                    .setEphemeral(true)
                    .queue();
            return;
        }
        event.reply("Welcome message is: " + message)
                .setEphemeral(true)
                .queue();
    }

    private void setWelcomeMessageHandler(@NotNull MessageContextInteractionEvent event) {
        Guild guild = event.getGuild();
        if(guild == null){
            return;
        }
        String message = event.getTarget().getContentRaw();
        Path jsonConfig = Path.of(guild.getName() + ".json");
        configService.setWelcomeMessage(jsonConfig, message);
        event.reply("Welcome message has been set successfully")
                .setEphemeral(true)
                .queue();
    }

    private void showRolesHandler(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if(guild == null){
            return;
        }
        Path jsonConfig = Path.of(guild.getName() + ".json");
        List<Role> roleList = configService.getRoles(jsonConfig, guild);
        if (roleList == null || roleList.isEmpty()) {
            event.reply("No roles set").setEphemeral(true).queue();
            return;
        }
        String roles = roleList.stream().map(IMentionable::getAsMention).collect(Collectors.joining(", ", "", ""));
        event.reply(roles).setEphemeral(true).queue();
    }

    private void addRoleHandler(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if(guild == null){
            return;
        }
        Path jsonConfig = Path.of(guild.getName() + ".json");
        Role newRole = Objects.requireNonNull(event.getOption("role")).getAsRole();
        boolean success = configService.addRole(jsonConfig, newRole, guild);
        if (!success) {
            event.reply("Role is already set").setEphemeral(true).queue();
            return;
        }
        event.reply("Added " + newRole.getAsMention() + " successfully").setEphemeral(true).queue();
    }

    private void removeRoleHandler(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if(guild == null){
            return;
        }
        Path jsonConfig = Path.of(guild.getName() + ".json");
        Role roleToRemove = Objects.requireNonNull(event.getOption("role")).getAsRole();
        boolean success = configService.removeRole(jsonConfig, roleToRemove, guild);
        if (!success) {
            event.reply("Given role was not present").setEphemeral(true).queue();
            return;
        }
        event.reply("Removed " + roleToRemove.getAsMention() + " successfully").setEphemeral(true).queue();
    }

    private void setGamesCategoryHandler(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if(guild == null){
            return;
        }
        Path jsonConfig = Path.of(guild.getName() + ".json");
        GuildChannelUnion channelUnion = Objects.requireNonNull(event.getOption("category")).getAsChannel();
        if (!channelUnion.getType().equals(ChannelType.CATEGORY)){
            event.reply("You need to pass category not a channel!").setEphemeral(true).queue();
            return;
        }
        Category category = channelUnion.asCategory();
        configService.setCategory(jsonConfig, category, guild);
        event.reply("Successfully set games category!").setEphemeral(true).queue();
    }

    private void showGamesCategoryHandler(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if(guild == null){
            return;
        }
        Path jsonConfig = Path.of(guild.getName() + ".json");
        Category category = configService.getGamesCategory(jsonConfig, guild);
        if (category == null) {
            event.reply("No games category set").setEphemeral(true).queue();
            return;
        }
        event.reply("Category is set to: " + category.getAsMention()).setEphemeral(true).queue();
    }

    private void setBettingChannelHandler(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if(guild == null){
            return;
        }
        Path jsonConfig = Path.of(guild.getName() + ".json");
        TextChannel channel = Objects.requireNonNull(event.getOption("betting-channel")).getAsChannel().asTextChannel();
        configService.setChannel(jsonConfig, "betting_channel_id", channel, guild);
        event.reply("Successfully set betting channel!").setEphemeral(true).queue();
    }

    private void showBettingChannelHandler(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        if(guild == null){
            return;
        }
        Path jsonConfig = Path.of(guild.getName() + ".json");
        TextChannel channel = configService.getChannel(jsonConfig, "betting_channel_id", guild);
        if (channel == null) {
            event.reply("No betting channel set").setEphemeral(true).queue();
            return;
        }
        event.reply("Betting channel is set to: " + channel.getAsMention()).setEphemeral(true).queue();
    }

    private String fillPlayersPlaceholders(String message, Member playerA, Member playerB) {
        message = message.replaceAll("\\{playerA}", playerA.getAsMention());
        message = message.replaceAll("\\{playerB}", playerB.getAsMention());
        return message;
    }
}