package com.packt.services.impl;

import com.packt.models.Game;
import com.packt.services.BettingService;
import com.packt.services.ConfigService;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.utils.messages.MessagePollData;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class BettingServiceImpl implements BettingService {
    private final ConfigService configService = new ConfigServiceImpl();

    @Override
    public void closePoll(@NotNull MessageContextInteractionEvent event) {
        event.getTarget().endPoll().queue();
    }

    @Override
    public boolean createPoll(@NotNull MessageContextInteractionEvent event) throws NumberFormatException {
        String bettingMsg = event.getTarget().getContentRaw();
        String[] lines = bettingMsg.split("\\R");
        List<Game> games = Game.getGamesList(lines);
        Guild guild = event.getGuild();
        if (guild == null) {
            return false;
        }
        TextChannel channel = configService.getChannel(Path.of(guild.getName() + ".json"),
                "betting_channel_id", guild);
        if (channel == null) {
            return false;
        }
        games.forEach(game -> channel.sendMessagePoll(
                        MessagePollData.builder("%s vs %s".formatted(game.playerA(), game.playerB()))
                                .addAnswer("%.02f - %s".formatted(game.scoreA(), game.playerA()))
                                .addAnswer("%.02f - %s".formatted(game.scoreB(), game.playerB()))
                                .setDuration(Duration.ofDays(7))
                                .build()
                ).queue()
        );
        return true;
    }
}
