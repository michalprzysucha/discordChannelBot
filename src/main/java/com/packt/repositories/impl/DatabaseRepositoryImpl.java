package com.packt.repositories.impl;

import com.packt.exceptions.DeletingPlayerWithGamesAssociationException;
import com.packt.models.DomainObject;
import com.packt.repositories.DatabaseRepository;
import com.packt.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class DatabaseRepositoryImpl implements DatabaseRepository<DomainObject> {
    protected final static SessionFactory sessionFactory = HibernateUtil.getSessionFactory();

    public DatabaseRepositoryImpl() {
    }

    @Override
    public void save(DomainObject domainObject) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.persist(domainObject);
            transaction.commit();

        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void delete(DomainObject domainObject) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.remove(domainObject);
            transaction.commit();
        } catch (DeletingPlayerWithGamesAssociationException e){
            if (transaction != null) {
                transaction.rollback();
            }
            throw e;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void update(DomainObject domainObject) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = null;
        try {
            transaction = session.beginTransaction();
            session.merge(domainObject);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }
}
