package hellojpa;

import jakarta.persistence.*;

import java.util.List;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        //code

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            // 비영속
//            Member member = new Member();
//            member.setId(100L);
//            member.setName("hello블라블라");
//
//            // 영속
//            System.out.println("===Before===");
//            em.persist(member);
//            System.out.println("===After===");

//            Member findMember = em.find(Member.class, 100L);

//            Member findMember2 = em.find(Member.class, 2L);
//            Member findMember3 = em.find(Member.class, 2L);
//
//            System.out.println("result: " + (findMember2 == findMember3));

//            Member member1 = new Member(4L, "hello4");
//            Member member2 = new Member(5L, "hello5");
//
//            em.persist(member1);
//            em.persist(member2);
//
//            System.out.println("=================================");

//            Member member5 = em.find(Member.class, 4L);
//            member5.setName("em.persist() 를 선언해줘야 하는거 아니야?");

//            em.persist(member5);

//            Member member1 = new Member(10L, "hello4");
//            Member member2 = new Member(11L, "hello5");
//            Member member3 = new Member(12L, "hello5");
////
//            em.persist(member1);
//            em.persist(member2);
//            em.persist(member3);
//
////중간에 JPQL 실행
//            List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();;
//
//            for (Member member : members) {
//                System.out.println(member.getName());
//            }

//            Member member = em.find(Member.class, 1L);
//            member.setName("AAAAAA");
//
//            em.detach(member);
//
//            System.out.println("===============");
//
//            tx.commit();

            Member member = em.find(Member.class, 1L);
            member.setName("AAAAAA");

            em.clear();

            Member member2 = em.find(Member.class, 2L);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
