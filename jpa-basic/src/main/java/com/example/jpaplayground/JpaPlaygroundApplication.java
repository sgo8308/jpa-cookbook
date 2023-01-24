package com.example.jpaplayground;

import com.example.jpaplayground.basic.Member;
import com.example.jpaplayground.basic.Team;
import com.example.jpaplayground.inheritance.Book;
import com.example.jpaplayground.valuetype.Address;
import com.example.jpaplayground.valuetype.AddressEntity;
import com.example.jpaplayground.valuetype.User;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

//@SpringBootApplication

/**
 * #jpabasic EntityManager 사용 방식
 */
public class JpaPlaygroundApplication {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();

        tx.begin();
        try {
            Member member = new Member();

            em.persist(member);

            tx.commit();
        } catch (Exception e ){
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }

}
