package com.packt.models;

import java.util.Arrays;
import java.util.List;

public record Game(String playerA, double scoreA, String playerB, double scoreB) {
    public static List<Game> getGamesList(String... lines) throws NumberFormatException {
        List<Game> games;
        games = Arrays.stream(lines)
                .map(String::trim)
                .map(line -> line.split(" "))
                .map(split -> new Game(split[0], Double.parseDouble(split[1]),
                        split[2], Double.parseDouble(split[3])))
                .toList();

        return games;
    }
}
