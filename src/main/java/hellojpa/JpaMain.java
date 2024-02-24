package hellojpa;

import jakarta.persistence.*;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        //code

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
//          Member member = new Member();
//          member.setName("User1");
//          member.setCreatedBy("Hyung Geol");
//          member.setCreatedAt(LocalDateTime.now());
//
//          em.persist(member);

          Parent parent = new Parent();
          ParentId parentId = new ParentId("parentId1", "parentId2");
          parent.setId(parentId);
          parent.setName("부모다");

          em.persist(parent);

          Child child1 = new Child();
          child1.setParent(parent);
          child1.setName("자식1");

          em.persist(child1);

          Child child2 = new Child();
          child1.setParent(parent);
          child1.setName("자식2");

          em.persist(child2);

          System.out.println(parent);

          em.flush();
          em.clear();

          Parent parent2 = em.find(Parent.class, child1.getParent().getId());

          tx.commit();
        } catch (Exception e) {
          System.out.println(e.getLocalizedMessage());
          System.out.println(e.getMessage());
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
