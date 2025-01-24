package com.packt.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "game_match")
public class GameMatch implements DomainObject {
    @Id
    @GeneratedValue
    private int id;
    @ManyToOne
    @JoinColumn(name = "playerA_id", nullable = false)
    private Player playerA;
    @ManyToOne
    @JoinColumn(name = "playerB_id", nullable = false)
    private Player playerB;
    @Column(name = "playerAScore")
    private int playerAScore;
    @Column(name = "playerBScore")
    private int playerBScore;
    @Column(name = "playerARatingChange")
    private double playerARatingChange;
    @Column(name = "playerBRatingChange")
    private double playerBRatingChange;
    @Column(name = "matchDate")
    private LocalDateTime matchDate;

    public GameMatch() {
    }

    public GameMatch(Player playerA, Player playerB, int playerAScore, int playerBScore) {
        if (playerA == playerB) {
            throw new IllegalArgumentException("Players are equal");
        }
        this.playerA = playerA;
        this.playerB = playerB;
        this.playerAScore = playerAScore;
        this.playerBScore = playerBScore;
        this.matchDate = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Player getPlayerA() {
        return playerA;
    }

    public void setPlayerA(Player playerA) {
        this.playerA = playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public void setPlayerB(Player playerB) {
        this.playerB = playerB;
    }

    public int getPlayerAScore() {
        return playerAScore;
    }

    public void setPlayerAScore(int playerAScore) {
        this.playerAScore = playerAScore;
    }

    public int getPlayerBScore() {
        return playerBScore;
    }

    public void setPlayerBScore(int playerBScore) {
        this.playerBScore = playerBScore;
    }

    public LocalDateTime getMatchDate() {
        return matchDate;
    }

    public void setMatchDate(LocalDateTime matchDate) {
        this.matchDate = matchDate;
    }

    public double getPlayerARatingChange() {
        return playerARatingChange;
    }

    public void setPlayerARatingChange(double playerARatingChange) {
        this.playerARatingChange = playerARatingChange;
    }

    public double getPlayerBRatingChange() {
        return playerBRatingChange;
    }

    public void setPlayerBRatingChange(double playerBRatingChange) {
        this.playerBRatingChange = playerBRatingChange;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        GameMatch gameMatch = (GameMatch) o;
        return id == gameMatch.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
