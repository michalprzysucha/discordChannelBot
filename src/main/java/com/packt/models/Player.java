package com.packt.models;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "player")
public class Player implements DomainObject {
    @Id
    @Column(name = "name")
    private String name;
    @Column(name = "rating")
    private double rating;

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
