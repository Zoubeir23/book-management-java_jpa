package sn.iage.isi.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("book_pu");

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}
