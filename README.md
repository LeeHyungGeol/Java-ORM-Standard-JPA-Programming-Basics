# 자바 ORM 표준 JPA 프로그래밍 - 기본편

# 1. Hello JPA - 애플리케이션 개발 시작

JPA 설정하기 
- persistence.xml
- JPA 설정 파일
- /META-INF/persistence.xml 위치
- persistence-unit name으로 이름 지정
- javax.persistence로 시작: JPA 표준 속성
- hibernate로 시작: 하이버네이트 전용 속성

데이터베이스 방언 - Dialect
![스크린샷 2024-01-29 오전 1.47.08.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_mck0L7%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-29%20%EC%98%A4%EC%A0%84%201.47.08.png)

JPA 구동 방식
![스크린샷 2024-01-29 오전 1.48.23.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_Dsp4VT%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-29%20%EC%98%A4%EC%A0%84%201.48.23.png)

```java
import hellojpa.Member;
import jakarta.persistence.*;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        //code

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            // 회원 생성
            Member member = new Member();
            member.setId(1L);
            member.setName("helloA");

            em.persist(member);
            
            // 회원 조회
            Member findMember = em.find(Member.class, 1L);
            System.out.println("id: " + findMember.getId());
            System.out.println("id: " + findMember.getName());
            
            // 회원 수정
            // 수정 시에는 em.persist() 를 호출 안해도 수정이 된다.
            // 이유는 JPA 를 통해서 Entity 를 가져올 시에 JPA 가 관리하게 된다.
            // JPA 가 변경 감지를 하고, tx 가 commit 하기 전에 update query 를 날리고, tx 을 commit 한다. 
            findMember.setName("helloJPA");
            
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
```

실제로는 위와 같은 try ~ catch 문이 필요하지 않다.
spring 이 다 해주기 때문에

* 수정 시에는 em.persist() 를 호출 안해도 수정이 된다.
* 이유는 JPA 를 통해서 Entity 를 가져올 시에 JPA 가 관리하게 된다.
* JPA 가 변경 감지를 하고, tx 가 commit 하기 전에 update query 를 날리고, tx 을 commit 한다.
- ***JPA의 모든 데이터 변경은 **Transaction** 안에서 실행***
- EntityManagerFactory 는 **하나**만 생성해서 **애플리케이션 전체에서 공유**
- EntityManager 는 쓰레드간에 공유X **(사용하고 버려야 한다)**.
- EntityManager 를 닫아 주는게 중요하다.
    - EntityManager 가 DB Connection 을 물고 동작하기 때문이다.

```java
import hellojpa.Member;
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
            // 회원 전체 조회
            List<Member> result = em.createQuery("select m from Member as m", Member.class)
                    .setFirstResult(1)
                    .setMaxResults(10)
                    .getResultList();
            
            for (Member m : result) {
                System.out.println("member.name = " + m.getName());
            }

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

        emf.close();
    }
}
```

JPQL 간단 예제
- JPQL 이라고 하는 것이 객체를 대상으로 하는 객체 지향 쿼리
- 방언(Dialect) 에 맞춰서 여러 DB 에 맞게 번역을 해준다.
- *검색에 엄청난 이점*


JPA를 사용하면 엔티티 객체를 중심으로 개발
- 문제는 검색 쿼리
- 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색
- 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검
색 조건이 포함된 SQL이 필요


JPA는 SQL을 추상화한 JPQL이라는 객체 지향 쿼리 언어 제공
- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY,
HAVING, JOIN 지원
- **JPQL은 엔티티 객체를 대상으로 쿼리**
- **SQL은 데이터베이스 테이블을 대상으로 쿼리**


테이블이 아닌 **객체를 대상**으로 검색하는 객체 지향 쿼리
- SQL을 추상화해서 특정 데이터베이스 SQL에 의존X
- JPQL을 한마디로 정의하면 ***객체 지향 SQL***
- JPQL은 뒤에서 아주 자세히 다룸

# 영속성 관리 

## 영속성 컨텍스트

**JPA에서 가장 중요한 2가지**
- 객체와 관계형 데이터베이스 매핑하기 (Object Relational Mapping)
- 영속성 컨텍스트

EntityMangerFactory 와 EntityManager
![스크린샷 2024-01-29 오후 5.12.19.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_dxPiKf%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-29%20%EC%98%A4%ED%9B%84%205.12.19.png)

**영속성 컨텍스트** 
- JPA 를 이해하는데 가장 중요한 용어
- "Entity 를 영구 저장하는 환경" 이라는 뜻
- `EntityManager.persist(entity);`
  - DB 에 저장하는 것이 아닌 영속성 컨텍스트를 통해서 entity 를 영속화 한다는 뜻
  - entity 를 영속성 컨텍스트에 저장한다는 의미
- EntityManager 를 통해서 영속성 컨텍스트에 접근

Entity 의 생명주기
![스크린샷 2024-01-29 오후 6.40.15.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_C0j3SX%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-29%20%EC%98%A4%ED%9B%84%206.40.15.png)

![스크린샷 2024-01-29 오후 6.40.41.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_OqeRBc%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-29%20%EC%98%A4%ED%9B%84%206.40.41.png)

```java
import jakarta.persistence.*;

public class JpaMain {

    public static void main(String[] args) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        //code

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            // 비영속
            Member member = new Member();
            member.setId(100L);
            member.setName("hello블라블라");

            // 영속
            System.out.println("===Before===");
            em.persist(member);
            System.out.println("===After===");
            
            // 준영속: 영속성 컨택스트에서 지우는 행위
            em.detach(member);

            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        } finally {
            em.close();
        }

      emf.close();
    }
}
```

```
===Before===
===After===
Hibernate: 
    /* insert for
        hellojpa.Member */insert 
    into
        Member (name, id) 
    values
        (?, ?)
```

- 영속 상태가 된다고 해서 DB 에 query 가 날라가는게 아니다.
- transaction 을 commit 하는 시점에 영속성 컨텍스트 안에 있는 애가 DB 에 query 가 날라가게 된다.

### 영속성 컨텍스트의 이점
- 1차 캐시 동일성(identity) 보장
- 트랜잭션을 지원하는 쓰기 지연 (transactional write-behind)
- 변경 감지(Dirty Checking) 
- 지연 로딩(Lazy Loading)

#### 1차 캐시 동일성(identity) 보장
**1차 캐시에서 조회**
![스크린샷 2024-01-29 오후 7.38.39.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_23BnGS%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-29%20%EC%98%A4%ED%9B%84%207.38.39.png)
```java
// 비영속
Member member = new Member();
member.setId(100L);
member.setName("hello블라블라");

// 영속 -> 1차캐시에 저장됨
System.out.println("===Before===");
em.persist(member);
System.out.println("===After===");

// 1차 캐시에서 조회
Member findMember = em.find(Member.class, 100L);
```

![스크린샷 2024-01-29 오후 7.40.10.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_QvonB7%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-29%20%EC%98%A4%ED%9B%84%207.40.10.png)
```java
Member findMember2 = em.find(Member.class, 2L);
```

***사실 하나의 transaction 이 끝나면 영속성 컨택스트를 지운다.***

**영속 엔티티의 동일성 보장**
```java
Member findMember2 = em.find(Member.class, 2L);
Member findMember3 = em.find(Member.class, 2L);

System.out.println("result: " + (findMember2 == findMember3)); // result: true
```

- 1차 캐시로 반복 가능한 읽기(REPEATABLE READ) 등급의 트랜잭션 격리 수준을 데이터베이스가 아닌 애플리케이션 차원에서 제공

#### 트랜잭션을 지원하는 쓰기 지연 (transactional write-behind)
![스크린샷 2024-01-30 오전 12.11.46.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_ZsP55l%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-30%20%EC%98%A4%EC%A0%84%2012.11.46.png)

![스크린샷 2024-01-30 오전 12.12.00.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_sxA5KC%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-30%20%EC%98%A4%EC%A0%84%2012.12.00.png)
```java
Member member1 = new Member(4L, "hello4");
Member member2 = new Member(5L, "hello5");

em.persist(member1);
em.persist(member2);

tx.commit();
```
- 1차 캐시에 저장
- JPA 가 entity 를 분석해서 insert SQL 을 생성하고, 쓰기 지연 SQL 저장소 라는 곳에 쌓아둔다.
- commit 하는 순간에 쓰기 지연 SQL 에 저장소에 있던 놈들을 DB 에 insert SQL 을 보낸다. JPA 에서는 *flush* 라고 한다.

```xml
<property name="hibernate.jdbc.batch_size" value="10"/>
```

- 버퍼링 같은 기능. batch_size 만큼 모아서 한방에 쿼리를 보낸다.
- 버퍼링을 모아서 write 하는 이점을 얻을 수 있다.

#### 엔티티 수정 및 삭제 - 변경 감지(Dirty Checking)

```java
Member member5 = em.find(Member.class, 4L);
member5.setName("em.persist() 를 선언해줘야 하는거 아니야?");

//em.persist(member5);

tx.commit();
```

![스크린샷 2024-01-30 오전 12.28.19.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_IpFtH4%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-30%20%EC%98%A4%EC%A0%84%2012.28.19.png)

- 1차 캐시 안에는 스냅샷이란 것이 있다.
  - 값을 읽어온 시점의 최초 시점의 상태를 말한다.
- 엔티티와 스냅샷을 비교한다.
- 마치 Java Collection 에서 하는 것 처럼 update 쿼리를 직접 날리지 않아도 ***DB 값이 변경된 것 감지(Dirty Checking)*** 해서 UPDATE 쿼리를 날려준다.
- **transaction 이 commit 하는 시점**에 값 변경을 감지하고 DB 에 반영을 해준다.




