package com.packt.services.impl;

import com.packt.models.GameMatch;
import com.packt.models.Player;
import com.packt.repositories.impl.DatabaseRepositoryPlayer;
import com.packt.services.RatingSystemService;
import com.packt.util.EloRatingCalculator;

import java.util.List;

public class RatingSystemServiceImpl implements RatingSystemService {
    DatabaseRepositoryPlayer playerRepository = new DatabaseRepositoryPlayer();

    @Override
    public boolean addPlayer(String playerName) {
        Player player = playerRepository.getPlayerByName(playerName);
        if (player == null) {
            player = new Player(playerName, 1500.0);
            playerRepository.save(player);
            return true;
        }
        return false;
    }

    @Override
    public boolean removePlayer(String playerName) {
        Player player = playerRepository.getPlayerByName(playerName);
        if (player == null) {
            return false;
        }
        playerRepository.delete(player);
        return true;
    }

    @Override
    public Player getPlayer(String playerName) {
        return playerRepository.getPlayerByName(playerName);
    }

    @Override
    public GameMatch saveMatchResult(String firstPlayerName, String secondPlayerName, int firstPlayerScore, int secondPlayerScore) {
        Player firstPlayer = getPlayer(firstPlayerName);
        Player secondPlayer = getPlayer(secondPlayerName);
        if (firstPlayer == null || secondPlayer == null) {
            return null;
        }
        GameMatch gameMatch = new GameMatch(firstPlayer, secondPlayer, firstPlayerScore, secondPlayerScore);
        EloRatingCalculator.updateRatings(gameMatch);
        playerRepository.update(firstPlayer);
        playerRepository.update(secondPlayer);
        playerRepository.save(gameMatch);
        return gameMatch;
    }

    @Override
    public List<Player> getAllPlayers() {
        return playerRepository.getAllPlayers();
    }
}
