package hellojpa;

import jakarta.persistence.*;
import org.hibernate.Hibernate;import java.util.List;

public class JpaMain {

    public static void main(String[] args) {

      EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
      EntityManager em = emf.createEntityManager();
      //code

      EntityTransaction tx = em.getTransaction();
      tx.begin();

      try {
        Team team1 = new Team();
        team1.setName("team1");
        em.persist(team1);

        Team team2 = new Team();
        team2.setName("team1");
        em.persist(team2);

        Member member1 = new Member();
        member1.setName("member1");
        member1.setTeam(team1);
        em.persist(member1);

        Member member2 = new Member();
        member2.setName("member2");
        member2.setTeam(team2);
        em.persist(member2);

        em.flush();
        em.clear();

//        Member m = em.find(Member.class, member1.getId());

        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();

        tx.commit();
      } catch (Exception e) {
        e.printStackTrace();
        tx.rollback();
      } finally {
        em.close();
      }

      emf.close();
    }

  private static void equalCompare(Member m1, Member m2) {
    System.out.println("m1 == m2: " + (m1.getClass() == m2.getClass()));
  }

  private static void instanceOfCompare(Member m1, Member m2) {
    System.out.println("m1 == m2: " + (m1 instanceof Member));
    System.out.println("m1 == m2: " + (m2 instanceof Member));
  }

  private static void printMember(Member member) {
    System.out.println("member = " + member.getName());
  }

  private static void printMemberAndTeam(Member member) {
      String userName = member.getName();
      System.out.println("userName = " + userName);

      Team team = member.getTeam();
      System.out.println("userName = " + team.getName());
  }
}
