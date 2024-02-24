package hellojpa;

import jakarta.persistence.*;
import org.hibernate.Hibernate;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        //code

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
          Member member1 = new Member();
          member1.setName("member1");
          em.persist(member1);

          em.flush();
          em.clear();

          Member refMember = em.getReference(Member.class, member1.getId());

//          em.detach(refMember);
// em.clear();
// em.close(); // 3가지 메서드 모두 exception 발생

          System.out.println("refMember = " + refMember);


          Member member2 = new Member();
          member2.setName("member2");
          em.persist(member2);

          Member member3 = new Member();
          member3.setName("member3");
          em.persist(member3);

          em.flush();
          em.clear();

          System.out.println("member2.getId() = " + member2.getId());
          System.out.println("member2.getName() = " + member2.getName());

//          Member findMember = em.find(Member.class, member.getId());
          Member findReference = em.getReference(Member.class, member1.getId());

          System.out.println("before findMember = " + findReference.getClass());
          System.out.println("findMember.id = " + findReference.getId());
          System.out.println("findMember.name  = " + findReference.getName());
          System.out.println("after findMember = " + findReference.getClass());

          em.flush();
          em.clear();

          Member m1 = em.find(Member.class, member1.getId());
          Member m2 = em.getReference(Member.class, member2.getId());

          instanceOfCompare(m1, m2);

//          printMember(member);

//          printMemberAndTeam(member);

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
