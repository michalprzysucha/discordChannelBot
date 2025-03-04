package com.packt.exceptions;

public class DeletingPlayerWithGamesAssociationException extends RuntimeException{
    public DeletingPlayerWithGamesAssociationException(){
        super("Can't remove a player that has played at least one match.");
    }
}
