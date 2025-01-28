package com.packt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packt.controllers.CommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main {
    public static String BOT_ID;

    public static void main(String[] arguments) throws Exception {
        String token;
        InputStream input = Main.class.getClassLoader().getResourceAsStream("config.json");
        if (input == null) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            ObjectMapper objectMapper = new ObjectMapper();
            token = objectMapper.readTree(reader).get("token").asText();
        }

        JDA api = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new CommandListener())
                .build();

        BOT_ID = api.getSelfUser().getId();
        CommandListUpdateAction commands = api.updateCommands();

        commands.addCommands(
                        // Creating text channels
                        Commands.slash("create-channel", "Creates a new match channel")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS))
                                .addOption(OptionType.USER, "first-player", "First player in a match", true)
                                .addOption(OptionType.USER, "second-player", "Second player in a match", true),
                        Commands.slash("show-roles", "Shows roles to add to channels created by this bot")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("add-role", "Adds role which will be added by bot to channels created by it")
                                .addOption(OptionType.ROLE, "role", "Role to add", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("remove-role", "Removes role which would be added by bot to channels created by it")
                                .addOption(OptionType.ROLE, "role", "Role to remove", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("show-welcome-message", "Shows message which will be sent by bot after channel creation")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("set-games-category", "Sets category in which match channels will be created")
                                .addOption(OptionType.CHANNEL, "category", "Category for match channels", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("show-games-category", "Shows category in which match channels will be created")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),

                        // Rating system
                        Commands.slash("add-player", "Register player to the ranking")
                                .addOption(OptionType.STRING, "player-name", "Player to add to the ranking", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("remove-player", "Removes player from the ranking system")
                                .addOption(OptionType.STRING, "player-name", "Player to be removed from the ranking", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("publish-ratings", "Publishes players and their ratings")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("save-match-result", "Saves match result and updates players' ratings")
                                .addOption(OptionType.STRING, "first-player-name", "First player in the match", true)
                                .addOption(OptionType.STRING, "second-player-name", "Second player in the match", true)
                                .addOption(OptionType.INTEGER, "first-player-score", "Games won in the match by first player", true)
                                .addOption(OptionType.INTEGER, "second-player-score", "Games won in the match by second player", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("set-rating-channel", "Sets channel in which players ratings will be posted")
                                .addOption(OptionType.CHANNEL, "rating-channel", "Channel to host rating standings", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("show-rating-channel", "Shows channel in which players ratings will be posted")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        // Betting
                        Commands.slash("set-betting-channel", "Sets betting channel which will be used to host betting polls")
                                .addOption(OptionType.CHANNEL, "betting-channel", "Betting channel", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("show-betting-channel", "Shows betting channel which will be used to host betting polls")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.message("Create betting polls")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.message("Close poll")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.message("Set welcome message")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS))
                )
                .queue();
    }
}