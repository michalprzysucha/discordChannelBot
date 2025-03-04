package com.packt.models;

import com.packt.exceptions.DeletingPlayerWithGamesAssociationException;
import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "player")
public class Player implements DomainObject {
    private static Logger logger = LoggerFactory.getLogger(DomainObject.class);

    @Id
    @Column(name = "name")
    private String name;
    @Column(name = "rating")
    private double rating;

    @OneToMany(mappedBy = "playerA", cascade = CascadeType.ALL)
    private List<GameMatch> gameMatchesAsPlayerA;

    @OneToMany(mappedBy = "playerB", cascade = CascadeType.ALL)
    private List<GameMatch> gameMatchesAsPlayerB;

    @PreRemove
    public void checkForGameMatches() {
        if (!gameMatchesAsPlayerA.isEmpty() || !gameMatchesAsPlayerB.isEmpty()) {
            throw new DeletingPlayerWithGamesAssociationException();
        }
    }

    public Player() {
        this.name = "default";
        this.rating = 1500;
    }

    public Player(String name, double rating) {
        if (rating < 1000){
            throw new IllegalArgumentException("Rating must be greater or equal 1000");
        }
        this.name = name;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        if (rating < 1000){
            throw new IllegalArgumentException("Rating must be a greater or equal 1000");
        }
        this.rating = rating;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Player player = (Player) o;
        return Objects.equals(name, player.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "%s %f".formatted(name, rating);
    }
}
