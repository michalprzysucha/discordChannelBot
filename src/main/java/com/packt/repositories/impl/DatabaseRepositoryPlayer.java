package com.packt.repositories.impl;

import com.packt.models.Player;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class DatabaseRepositoryPlayer extends DatabaseRepositoryImpl {
    public Player getPlayerByName(String name){
        Transaction transaction = null;
        Player player = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            player = session.get(Player.class, name);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
        return player;
    }

    public List<Player> getAllPlayers() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Player", Player.class).list();
        }
    }
}
