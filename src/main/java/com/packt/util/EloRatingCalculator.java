package com.packt.util;

import com.packt.models.GameMatch;
import com.packt.models.Player;

public class EloRatingCalculator {
    private static final double C = 400;
    private static final double K = 40;

    public static void updateRatings(GameMatch match){
        Player playerA = match.getPlayerA();
        Player playerB = match.getPlayerB();
        double ratingA = playerA.getRating();
        double ratingB = playerB.getRating();
        int scoreA = match.getPlayerAScore();
        int scoreB = match.getPlayerBScore();
        if (scoreA == scoreB && scoreA == 0){
            return;
        }
        double scoreRatioA = (double) scoreA / (scoreA + scoreB);
        double scoreRatioB = (double) scoreB / (scoreB + scoreA);
        double expectedOutcomeA = Ea(q(ratingA), q(ratingB));
        double expectedOutcomeB = Ea(q(ratingB), q(ratingA));
        int scoreDifference = scoreDifference(scoreA, scoreB);
        double g = g(scoreDifference, ratingA, ratingB);
        double playerANewRating =  ratingA + K * g * (scoreRatioA - expectedOutcomeA);
        double playerBNewRating =  ratingB + K * g * (scoreRatioB - expectedOutcomeB);
        playerA.setRating(Math.max(playerANewRating, 1000));
        playerB.setRating(Math.max(playerBNewRating, 1000));
        match.setPlayerARatingChange(playerA.getRating() - ratingA);
        match.setPlayerBRatingChange(playerB.getRating() - ratingB);
    }

    private static double Ea(double qPlayerA, double qPlayerB){
        return qPlayerA / (qPlayerA + qPlayerB);
    }

    private static double q(double rating){
        return Math.pow(10, (rating / C));
    }

    private static int scoreDifference(int scoreA, int scoreB){
        return Math.abs(scoreA - scoreB);
    }

    private static double g(int scoreDifference, double ratingA, double ratingB){
        return Math.log(scoreDifference + 1) * (2.2 / ((ratingA - ratingB) * 0.001 + 2.2));
    }
}

