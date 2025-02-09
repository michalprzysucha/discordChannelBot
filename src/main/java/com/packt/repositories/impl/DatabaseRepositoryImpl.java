package com.packt.repositories.impl;

import com.packt.models.DomainObject;
import com.packt.repositories.DatabaseRepository;
import com.packt.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class DatabaseRepositoryImpl implements DatabaseRepository<DomainObject> {
    protected final SessionFactory sessionFactory;

    public DatabaseRepositoryImpl() {
        this.sessionFactory = HibernateUtil.getSessionFactory(HibernateUtil.getSqliteConfiguration());
    }

    @Override
    public void save(DomainObject domainObject) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
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
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.remove(domainObject);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
    }

    @Override
    public void update(DomainObject domainObject) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
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
