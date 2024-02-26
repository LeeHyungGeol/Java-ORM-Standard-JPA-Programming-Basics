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
        CascadeChild child1 = new CascadeChild();
        CascadeChild child2 = new CascadeChild();

        CascadeParent parent = new CascadeParent();
        parent.addChild(child1);
        parent.addChild(child2);

        em.persist(parent);

        em.flush();
        em.clear();

        System.out.println("===== select CascadeParent =====");
        CascadeParent p = em.find(CascadeParent.class, parent.getId());
        System.out.println("===== select CascadeParent =====");

        System.out.println("===== delete CascadeChild =====");
        em.remove(p);
        System.out.println("===== delete CascadeChild =====");

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
