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
        Address address = new Address("city", "street", "100000");

        Member member1 = new Member();
        member1.setName("member1");
        member1.setHomeAddress(address);
        em.persist(member1);

        Address copyAddress = new Address(address.getCity(), address.getStreet(), address.getZipcode());

        Member member2 = new Member();
        member2.setName("member1");
        member2.setHomeAddress(copyAddress);
        em.persist(member2);


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
