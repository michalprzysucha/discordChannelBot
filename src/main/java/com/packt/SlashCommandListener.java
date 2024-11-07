package com.packt;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getName()) {
            case "say" ->
                event.reply(event.getOption("content").getAsString()).queue(); // reply immediately
            case "createchannel" -> {
                Guild guild = event.getGuild();
                if (guild != null) {
                    guild.createTextChannel("new-text-channel").queue(
                            (TextChannel channel) -> {
                                // Send a reply to the user that the channel has been created
                                event.reply("Text channel `" + channel.getName() + "` has been created!").queue();
                            },
                            (Throwable error) -> {
                                // Handle any error that occurs during channel creation
                                event.reply("Failed to create the channel.").setEphemeral(true).queue();
                            }
                    );
                } else {
                    event.reply("This command can only be used in a server.").setEphemeral(true).queue();
                }
            }
            // Handle other commands here if needed
            default -> event.reply("Unknown command!").setEphemeral(true).queue();
        }
    }
}

