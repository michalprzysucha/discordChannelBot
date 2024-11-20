package com.packt;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Main {
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
                .addEventListeners(new SlashCommandListener())
                .build();

        CommandListUpdateAction commands = api.updateCommands();

        commands.addCommands(
                        Commands.slash("create-channel", "Creates a new match channel")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS))
                                .addOption(OptionType.USER, "first-player", "First player in a match", true)
                                .addOption(OptionType.USER, "second-player", "Second player in a match", true),
                        Commands.slash("show-roles", "Show roles to add to channels created by this bot")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("add-role", "Add role which will be added by bot to channels created by it")
                                .addOption(OptionType.ROLE, "role", "Role to add", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("remove-role", "Removes role which would be added by bot to channels created by it")
                                .addOption(OptionType.ROLE, "role", "Role to remove", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("set-welcome-message", "Sets message which will be sent by bot after channel creation")
                                .addOption(STRING, "message", "Welcome message to set", true)
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.slash("show-welcome-message", "Shows message which will be sent by bot after channel creation")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.message("Create betting polls")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS)),
                        Commands.message("Close poll")
                                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_CHANNEL, Permission.MODERATE_MEMBERS))
                )
                .queue();
    }
}