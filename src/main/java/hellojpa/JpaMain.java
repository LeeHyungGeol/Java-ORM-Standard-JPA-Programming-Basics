package hellojpa;

import jakarta.persistence.*;

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
      Member member = new Member();
      member.setName("member1");
      member.setHomeAddress(new Address("new", "street", "100000"));

      member.getFavoriteFoods().add("국밥");
      member.getFavoriteFoods().add("돈까스");
      member.getFavoriteFoods().add("햄버거");

      member.getAddressHistory().add(new AddressEntity("old1", "street", "100000"));
      member.getAddressHistory().add(new AddressEntity("old2", "street", "100000"));

      em.persist(member);

      em.flush();
      em.clear();

      Member findMember = em.find(Member.class, member.getId());

      // 치킨 -> subway
      findMember.getFavoriteFoods().remove("국밥");
      findMember.getFavoriteFoods().add("subway");

      // old1 -> oldCity
      findMember.getAddressHistory().remove(new AddressEntity("old1", "street", "100000"));
      findMember.getAddressHistory().add(new AddressEntity("oldCity", "street", "100000"));


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
