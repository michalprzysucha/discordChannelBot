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
        double pA = match.getPlayerAScore();
        double pB = match.getPlayerBScore();
        if (pA == pB && pA == 0){
            return;
        }
        double scoreRatioA = pA / (pA + pB);
        double scoreRatioB = pB / (pB + pA);
        double expectedOutcomeA = Ea(q(ratingA), q(ratingB));
        double expectedOutcomeB = Ea(q(ratingB), q(ratingA));
        double playerANewRating =  ratingA + K * (scoreRatioA - expectedOutcomeA);
        double playerBNewRating =  ratingB + K * (scoreRatioB - expectedOutcomeB);
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

    private static double s(double pA, double pB){
        if(pA > pB){
            return 1.0;
        }
        else if(pA == pB){
            return 0.5;
        }
        else{
            return 0.0;
        }
    }
}

