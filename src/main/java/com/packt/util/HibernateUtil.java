package com.packt.util;

import com.packt.models.GameMatch;
import com.packt.models.Player;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class HibernateUtil {
    private static SessionFactory sessionFactory;
    public static SessionFactory getSessionFactory(Configuration config) {
        if (sessionFactory == null) {
            try {
                config.addAnnotatedClass(Player.class);
                config.addAnnotatedClass(GameMatch.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(config.getProperties()).build();

                sessionFactory = config.buildSessionFactory(serviceRegistry);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }

    public static Configuration getMysqlConfiguration() {
        Configuration configuration = new Configuration();

        Properties settings = new Properties();
        settings.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
        settings.put(Environment.URL, "jdbc:mysql://localhost:3306/7k_rating?useSSL=false");
        settings.put(Environment.USER, "root");
        settings.put(Environment.PASS, "root");
        settings.put(Environment.SHOW_SQL, "true");

        settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

//        Uncomment only on first usage!
//        settings.put(Environment.HBM2DDL_AUTO, "create-drop");

        configuration.setProperties(settings);
        return configuration;
    }

    public static Configuration getSqliteConfiguration() {
        Configuration configuration = new Configuration();

        Properties settings = new Properties();
        settings.put("hibernate.connection.driver_class", "org.sqlite.JDBC");
        settings.put("hibernate.connection.url", "jdbc:sqlite:7k_rating.db");
        settings.put("hibernate.show_sql", "true");
        settings.put("hibernate.format_sql", "true");

        settings.put(Environment.CURRENT_SESSION_CONTEXT_CLASS, "thread");

//        Uncomment only on first usage!
//        settings.put(Environment.HBM2DDL_AUTO, "create-drop");

        configuration.setProperties(settings);
        return configuration;
    }
}
