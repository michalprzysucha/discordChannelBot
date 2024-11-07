package com.packt;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.io.FileReader;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class Main {
    public static void main(String[] arguments) throws Exception {
        String token;
        try(FileReader reader = new FileReader("config.json")){
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            token = jsonObject.get("token").getAsString();
        }
        JDA api = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(new SlashCommandListener())
                .build();

        CommandListUpdateAction commands = api.updateCommands();

        // Add all your commands on this action instance
        commands.addCommands(
                Commands.slash("say", "Makes the bot say what you tell it to")
                        .addOption(STRING, "content", "What the bot should say", true),
                Commands.slash("createchannel", "Creates a new channel")
        );
        commands.queue();
    }
}