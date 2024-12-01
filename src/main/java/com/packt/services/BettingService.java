package com.packt.services;

import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import org.jetbrains.annotations.NotNull;

public interface BettingService {
    void closePoll(@NotNull MessageContextInteractionEvent event);
    boolean createPoll(@NotNull MessageContextInteractionEvent event);
}
