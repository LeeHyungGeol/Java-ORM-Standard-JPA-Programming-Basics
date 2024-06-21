# 2. 영속성 관리

## Index
- [영속성 컨텍스트](#영속성-컨텍스트)
  - [영속성 컨텍스트의 이점](#영속성-컨텍스트의-이점)
  - [1차 캐시 동일성(identity) 보장](#1차-캐시-동일성identity-보장)
  - [트랜잭션을 지원하는 쓰기 지연 (transactional write-behind)](#트랜잭션을-지원하는-쓰기-지연-transactional-write-behind)
  - [엔티티 수정 및 삭제 - 변경 감지(Dirty Checking)](#엔티티-수정-및-삭제---변경-감지dirty-checking)
- [플러시(flush)](#플러시flush)
- [준영속(detach)](#준영속detach)


## 영속성 컨텍스트

**JPA에서 가장 중요한 2가지**
- 객체와 관계형 데이터베이스 매핑하기 (Object Relational Mapping)
- 영속성 컨텍스트

EntityMangerFactory 와 EntityManager
<img width="703" alt="스크린샷 2024-02-10 오후 9 36 16" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/2f1bc354-8bfe-4234-8a69-ebbd2843a7e6">

**영속성 컨텍스트**
- JPA 를 이해하는데 가장 중요한 용어
- "Entity 를 영구 저장하는 환경" 이라는 뜻
- `EntityManager.persist(entity);`
  - DB 에 저장하는 것이 아닌 영속성 컨텍스트를 통해서 entity 를 영속화 한다는 뜻
  - entity 를 영속성 컨텍스트에 저장한다는 의미
- EntityManager 를 통해서 영속성 컨텍스트에 접근

<img width="707" alt="스크린샷 2024-02-10 오후 9 38 24" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/18a2bfa6-228d-4530-9d3b-cb767c1643c0">

**Entity 의 생명주기**
<img width="681" alt="스크린샷 2024-02-10 오후 9 35 11" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/c2d371d6-099c-48e9-bdbb-179ff2adf2af">

<img width="652" alt="스크린샷 2024-02-10 오후 9 39 42" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/d2f52207-19a8-4572-b230-1779792cf009">

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

<img width="650" alt="스크린샷 2024-02-10 오후 9 41 55" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/fc44d3e0-8ef7-4341-9ddc-ac81b28014fa">

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

<img width="886" alt="스크린샷 2024-02-10 오후 9 42 50" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/fba567c7-4c17-46ec-9a2c-bab39a0d4fb8">

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
<img width="887" alt="스크린샷 2024-02-10 오후 9 44 00" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/8b1350e2-273f-4428-b90b-9d29ff0fd724">

<img width="764" alt="스크린샷 2024-02-10 오후 9 44 22" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/4eafea85-9e8d-4bfe-9a4a-4f1b5b7756a8">

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
member5.setAge(20);

//em.persist(member5);

tx.commit();
```

<img width="835" alt="스크린샷 2024-02-10 오후 9 45 14" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/2b8f0b80-1f3f-447d-8cb6-66d2580dcd4c">

- 1차 캐시 안에는 스냅샷이란 것이 있다.
  - 값을 읽어온 시점의 최초 시점의 상태를 말한다.
- 엔티티와 스냅샷을 비교한다.
- 마치 Java Collection 에서 하는 것 처럼 update 쿼리를 직접 날리지 않아도 ***DB 값이 변경된 것 감지(Dirty Checking)*** 해서 UPDATE 쿼리를 날려준다.
- **transaction 이 commit 하는 시점**에 값 변경을 감지하고 DB 에 반영을 해준다.
  
#### 변경 감지로 인해 실행된 UPDATE SQL

**수정된 데이터만 반영할 것으로 예상**
```sql
UPDATE MEMBER
SET
  NAME=?
  AGE=?
WHERE
  id=?
```

하지만, **JPA 의 기본 전략은 Entity 의 모든 필드를 업데이트한다.**

**엔티티의 모든 필드를 수정**

```sql
UPDATE MEMBER
SET
  NAME=?
  AGE=?
WHERE
  id=?
```

이렇게 모든 필드를 사용하면 DB 에 보내는 데이터 전송량이 증가하는 단점이 있다. 하지만.

1. 모든 필드를 사용하면 update query 가 항상 같다.(물론, 바인딩 되는 데이터는 다르다)
   **따라서, 애플리케이션 로딩 시점에 update query 를 미리 생성해두고 재사용 가능하다.**
2. **DB 에 동일한 쿼리를 보내면 DB 는 이전에 한 번 파싱된 쿼리를 재사용할 수 있다.**

이러한 장점들로 인해 모든 필드를 업데이트한다.   


## 플러시(flush)

**플러시(flush): 영속성 컨텍스트의 변경내용을 DB 에 반영**
- insert, delete, update sql 들을 DB 에 반영하는 것
- 영속성 컨텍스트의 변경사항과 DB 를 맞추는 작업

플러시는!
1. **영속성 컨텍스트를 비우지 않음**
2. **영속성 컨텍스트의 변경내용을 데이터베이스에 동기화**
3. **트랜잭션이라는 작업 단위가 중요 -> 커밋 직전에만 동기화 하면 됨**

**플러시 발생**
- 변경 감지
- 수정된 엔티티 쓰기 지연 SQL 저장소에 등록
- 쓰기 지연 SQL 저장소의 쿼리를 데이터베이스에 전송 (등록, 수정, 삭제 쿼리)

영속성 컨텍스트를 플러시하는 방법

- em.flush() - 직접 호출
- 트랜잭션 커밋 - 플러시 자동 호출
- JPQL 쿼리 실행 - 플러시 자동 호출

**flush 를 한다고 해서 1차 캐시가 삭제되는 것은 아니다.** 단지, 변경 감지를 하고, 쓰기 지연 (write-behind) 저장소에 있는 sql 들을 DB 에 전송하는 역할을 한다.

**JPQL 쿼리 실행시 플러시가 자동 으로 호출되는 이유**

```java
em.persist(memberA);
em.persist(memberB);
em.persist(memberC);

//중간에 JPQL 실행
query = em.createQuery("select m from Member m", Member.class);
List<Member> members= query.getResultList();
```

- em.persist() 까지는 영속성 컨텍스트에 저장한 상태이지, DB 에 저장한 상태가 아니다.
- 그래서 원래는 조회가 안되는 상태다. DB 에서 가져올 것이 없는 상태이기 때문이다.
- 이런 상황에서 문제가 발생할 수 있기 때문에, JPQL 실행시에 무조건 flush() 가 자동 호출된다.
- **식별자를 기준으로 조회하는 find() 메서드를 호출할 때는 flush() 가 실행 되지 않는다.**

**플러시 모드 옵션**
`em.setFlushMode(FlushModeType.COMMIT)`
- 이렇게 플러시 모드 설정

`FlushModeType.AUTO`
- 커밋이나 쿼리를 실행할 때 플러시 (기본값)

`FlushModeType.COMMIT`
- 커밋할 때만 플러시

**flush : 영속성 컨텍스트에 있는 엔티티 정보를 DB에 동기화하는 작업. 아직 트랜잭션 commit이 안돼서 에러 발생시 롤백 가능**

**transaction commmit : transaction commit 이후에는 DB에 동기화된 정보가 영구히 반영되어 롤백을 할 수 없다.**

**트랜잭션 커밋과 플러시가 일어나는 시점**

- 보통 Repository를 만들 때 Spring-Data-Jpa의 JpaRepository 인터페이스를 상속받는다.
- 이때 JpaRepository 의 기본 구현체는 SimpleJpaRepository 이며 SimpleJpaRepository의 클래스에는 @Transactional 어노테이션이 붙어 있으므로 해당 클래스에 있는 메소드들은 모두 트랜잭션에 걸리게 된다.
- 해당 클래스에 있는 메소드들이 모두 트랜젝션에 걸린다는 것은 다시말해, 클래스의 메소드가 성공적으로 return 하게 되면 Transaction Commit 도 이루어지게 된다.
- JpaRepository 인터페이스 상속을 받지 않더라고 클래스에 @Transactional 어노테이션을 붙이게 되면 해당 클래스의 메소드 실행 return 후 Transaction Commit 이 실행된다.

- [참조] https://velog.io/@eeheaven/JPA-TIL-%EC%97%B0%EA%B4%80%EA%B4%80%EA%B3%84-%EB%A7%A4%ED%95%91

## 준영속(detach)

- 영속 -> 준영속
- 영속 상태(영속성 컨텍스트(안의 1차캐시)에 저장된 상태)의 엔티티가 영속성 컨텍스트에서 분리(detached)
- 영속성 컨텍스트가 제공하는 기능을 사용 못함

**준영속 상태로 만드는 방법**
- `em.detach(entity)`: 특정 엔티티만 준영속 상태로 전환
- `em.clear()`: 영속성 컨텍스트를 완전히 초기화
- `em.close()`: 영속성 컨텍스트를 종료

```java
Member member = em.find(Member.class, 1L);
member.setName("AAAAAA");

em.detach(member);

tx.commit();
```

```
Hibernate: 
    select
        m1_0.id,
        m1_0.name 
    from
        Member m1_0 
    where
        m1_0.id=?
===============
```

- 영속 상태에서 준영속 상태가 되었기 때문에 update query 가 날라가지 않는다.

```java
Member member = em.find(Member.class, 1L);
member.setName("AAAAAA");

em.clear();

Member member2 = em.find(Member.class, 2L);

tx.commit();
```

```
Hibernate: 
    select
        m1_0.id,
        m1_0.name 
    from
        Member m1_0 
    where
        m1_0.id=?
Hibernate: 
    select
        m1_0.id,
        m1_0.name 
    from
        Member m1_0 
    where
        m1_0.id=?
```

- 영속석 컨택스트(안의 1차 캐시)를 완전히 지워버렸기 때문에 select query 가 2번 날라가게 된다.


---
