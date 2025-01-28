package com.packt.services;

import com.packt.models.GameMatch;
import com.packt.models.Player;

import java.util.List;

public interface RatingSystemService {
    boolean addPlayer(String playerName);
    boolean removePlayer(String playerName);
    GameMatch saveMatchResult(String firstPlayerName, String secondPlayerName,
                              int firstPlayerScore, int secondPlayerScore);
    Player getPlayer(String playerName);
    List<Player> getAllPlayers();
}
