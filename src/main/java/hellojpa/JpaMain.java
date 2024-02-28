package hellojpa;

import jakarta.persistence.*;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.List;
import java.util.Set;

public class JpaMain {

  public static void main(String[] args) {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
    EntityManager em = emf.createEntityManager();
    //code

    EntityTransaction tx = em.getTransaction();
    tx.begin();

    try {
      List<Member> resultList = em.createNativeQuery("SELECT MEMBER_ID, city, street, zipcode, USERNAME, TEAM_ID FROM MEMBER WHERE USERNAME = 'lee'", Member.class).getResultList();

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
