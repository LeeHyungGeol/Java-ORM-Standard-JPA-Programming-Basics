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

# 2. 영속성 관리 

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

# 3. 엔티티 매핑

1. 영속성 컨텍스트, JPA 내부 동작 방식과 같은 **JPA 가 내부적으로 어떤 메커니즘으로 동작하는지 그런 메커니즘적인 측면**
2. **실제 설계적인 측면, 객체랑 관계형 데이터베이스(RDB)를 어떻게 mapping 을 해서 쓰는지에** 대한 정적인 측면

**엔티티 매핑 소개**
- 객체와 테이블 매핑: `@Entity`, `@Table` 
- 필드와 컬럼 매핑: `@Column`
- 기본 키 매핑: `@Id`
- 연관관계 매핑: `@ManyToOne`, `@JoinColumn`

## 객체와 테이블 매핑

### @Entity
- **@Entity가 붙은 클래스는 JPA가 관리, 엔티티라 한다.**
- JPA를 사용해서 테이블과 매핑할 클래스는 **@Entity** 필수

주의
- 기본 생성자 필수(파라미터가 없는 public 또는 protected 생성자)
- final 클래스, enum, interface, inner 클래스 사용 X
- 저장할 필드에 final 사용 X

#### @Entity 속성 정리

```java
import jakarta.persistence.Entity;

@Entity(name = "Member")
```

**속성: name**
- **JPA에서 사용할 엔티티 이름을 지정한다.**
- 기본값: 클래스 이름을 그대로 사용(예: Member) 
- 같은 클래스 이름이 없으면 가급적 기본값을 사용한다. (기본값을 사용 안하면 헷갈리는 경우가 많다.)

### @Table

```java
import jakarta.persistence.Table;

@Table(name = "Mbr")
```

#### @Table 속성 정리
- @Table은 엔티티와 매핑할 테이블 지정

| 속성                      | 기능                    | 기본값        |
|-------------------------|-----------------------|------------|
| name                    | 매핑할 테이블 이름            | 엔티티 이름을 사용 |
| catalog                 | 데이터베이스 catalog 매핑     |            |
| schema                  | 데이터베이스 schema 매핑      |            |
| uniqueConstraints (DDL) | DDL 생성 시에 유니크 제약 조건 생성 |            |

## 데이터베이스 스키마 자동 생성

**매핑 정보만 보면 어떤 테이블, 컬럼들이 필요한지 알 수 있기 때문에, JPA 에서 애플리케이션 로딩 시점에 DB 테이블을 생성하는 기능도 지원해준다.**
- 당연히 운영환경에서는 사용 안하고, 로컬이나 dev 에서 테스트할 때 보통 사용한다.

- DDL 을 애플리케이션 실행 시점에 자동 생성
  - 객체에다가 테이블 매핑 정보를 설정 해놓으면, JPA 에서 애플리케이션 실행 시점에 다 만들어준다.
- *테이블 중심 -> 객체 중심*
- **데이터베이스 방언(Dialect)을 활용해서 데이터베이스에 맞는 적절한 DDL 생성** 
- 이렇게 **생성된 DDL은 개발 장비(local, dev)에서만 사용** 
- 생성된 DDL은 운영서버에서는 사용하지 않거나, 적절히 다듬은 후 사용

```xml
<property name="hibernate.hbm2ddl.auto" value="create" />
```

```
Hibernate: 
    drop table if exists Mbr cascade 
Hibernate: 
    create table Mbr (
        age integer not null,
        id bigint not null,
        name varchar(255),
        primary key (id)
    )
Hibernate: 
    select
        m1_0.id,
        m1_0.name 
    from
        Mbr m1_0 
    where
        m1_0.id=?

```

**속성**

`hibernate.hbm2ddl.auto`

|옵션 |설명|
|---|---|
|create |기존테이블 삭제 후 다시 생성 (DROP + CREATE)|
|create-drop |create와 같으나 종료시점에 테이블 DROP|
|update |변경분만 반영(운영DB에는 사용하면 안됨)|
|validate |엔티티와 테이블이 정상 매핑되었는지만 확인|
|none |사용하지 않음|

- update: 컬럼 추가하는 것과 같은 update 일 때 동작, 컬럼 삭제하면 아무 일도 일어나지 않는다.


**주의**
- **운영 장비에는 절대 create, create-drop, update 사용하면 안된다.**
  - update 도 alter table query 가 날라가기 때문에 운영서버에서 동작하면 DB 에 Lock 이 걸린다.
- 개발 초기 단계는 create 또는 update
- 테스트 서버는 update 또는 validate
- 스테이징과 운영 서버는 validate 또는 none

**결론적으로, local pc 에서만 자유롭게 활용하고, 테스트 해보는거고, dev, qa, stage, prod 와 같은 서버에서는 가급적이면 사용하지 말자. 어떤 문제가 일어날지 모른다.**


**DDL 생성 기능**

```java
@Column(nullable = false, length = 10)
private String name;
```
- 제약조건 추가: 회원 이름은 필수, 10자 초과X
  - `@Column(nullable = false, length = 10)`
- 유니크 제약조건 추가
  - `@Table(uniqueConstraints = {@UniqueConstraint( name = "NAME_AGE_UNIQUE", columnNames = {"NAME", "AGE"} )})`
- **DDL 생성 기능은 DDL을 자동 생성할 때만 사용되고, JPA의 실행 로직에는 영향을 주지 않는다.**
  - validation 을 하는 경우도 있긴 하다.

## 필드와 컬럼 매핑

요구사항 추가
1. 회원은 일반회원과 관리자로 구분해야한다.
2. 회원 가입일과 수정일이 있어야 한다.
3. 회원을 설명할 수 있는 필드가 있어야 한다. 이 필드는 길이 제한이 없다.

```java
@Entity
public class Member {
    @Id
    private Long id;
    
    @Column(name = "name")
    private String username;
    
    private Integer age;
    
    @Enumerated(EnumType.STRING)
    private RoleType roleType;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;
    
    @Lob
    private String description;
    
    @Transient
    private int temp;
    
    // JPA 는 기본적으로 내부적으로 reflection API 를 쓰기 때문에 동적으로 객체를 생성해야 한다.
    // 따라서, 기본 생성자가 꼭 필요하다.
    public Member() {
    }
    
    // Getter, Setter...
}
```

```
Hibernate: 
    drop table if exists Member cascade 
Hibernate: 
    create table Member (
        age integer,
        createdDate timestamp(6),
        id bigint not null,
        lastModifiedDate timestamp(6),
        name varchar(255),
        roleType varchar(255) check (roleType in ('ADMIN','USER')),
        description clob,
        primary key (id)
    )
```

### 매핑 어노테이션 정리

`hibernate.hbm2ddl.auto`

|어노테이션| 설명|
|---|---|
|@Column| 컬럼 매핑|
|@Temporal|날짜 타입 매핑|
|@Enumerated|enum 타입 매핑|
|@Lob|BLOB, CLOB 매핑|
|@Transient|특정 필드를 컬럼에 매핑하지 않음(매핑 무시)|

- `@Lob`: DB 에 varchar 를 넘어서는 **굉장히 큰 컨텐츠**를 넣고 싶을 때 사용
- `@Transient`: DB 에 전혀 관계없이 메모리에서만 계산하고 싶을 때 사용 

#### @Column

```java
// DB 의 컬럼명은 name 이다.
@Column(name = "name", insertable = true, updatable = true, nullable = false, unique = true,
columnDefinition = "varchar(100) default ‘EMPTY'", length = 255)
private String userName;

@Column(precision = 10, scale = 10)
private BigDecimal age;
```

| 속성                                                            |설명 |기본값|
|---------------------------------------------------------------|---|---|
| name                                                          |필드와 매핑할 테이블의 컬럼 이름|객체의 필드 이름|
| insertable, updatable                                         |등록, 변경 가능 여부|TRUE|
| nullable(DDL)                                                 |null 값의 허용 여부를 설정한다. false로 설정하면 DDL 생성 시에 not null 제약조건이 붙는다.||
| unique(DDL)                                                   |@Table의 uniqueConstraints와 같지만 한 컬럼에 간단히 유니크 제약조건을 걸 때 사용한다.|| 
|columnDefinition (DDL)|데이터베이스 컬럼 정보를 직접 줄 수 있다. ex) varchar(100) default ‘EMPTY'|필드의 자바 타입과 방언 정보를 사용해||
|length(DDL)|문자 길이 제약조건, String 타입에만 사용한다.|255| 
|precision, scale(DDL)|BigDecimal 타입에서 사용한다(BigInteger도 사용할 수 있다). precision은 소수점을 포함한 전체 자 릿수를, scale은 소수의 자릿수다. 참고로 double, float 타입에는 적용되지 않는다. 아주 큰 숫자나 정밀한 소수를 다루어야 할 때만 사용한다.|precision=19, scale=2|


```
Hibernate: 
    alter table if exists Member 
       add constraint UK_ektea7vp6e3low620iewuxhlq unique (name)
```
- unique: JPA 가 만들어주는 유니크 이름이 읽기 힘든 값(UK_ektea7vp6e3low620iewuxhlq)이 나오기 때문에 unique 는 잘 사용하지 않는다. @Table 의 uniqueConstraints 를 사용한다.


#### @Enumerated
자바 enum 타입을 매핑할 때 사용 

***주의! ORDINAL 사용X***

|속성 |설명 |기본값|
|---|---|---|
|value|EnumType.ORDINAL: enum 순서를 데이터베이스에 저장, **EnumType.STRING: enum 이름을 데이터베이스에 저장**|EnumType.ORDINAL|


#### @Temporal
- 날짜 타입(java.util.Date, java.util.Calendar)을 매핑할 때 사용
- 참고: LocalDate, LocalDateTime을 사용할 때는 생략 가능(최신 하이버네이트 지원)

```java
private LocalDate testLocalDate;
private LocalDateTime testLocalDateTime;
```

```
testLocalDate date,
testLocalDateTime timestamp(6),
```
-  LocalDate 는 data, LocalDateTime 은 TimeStamp 타입으로 생성된다.
- Java 8 이전의 버젼(옛날 버젼)을 사용해야 한다면 `@Temporal` 을 사용하면 된다.
![스크린샷 2024-01-31 오후 4.47.35.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_Qkqijp%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-31%20%EC%98%A4%ED%9B%84%204.47.35.png)

#### @Lob
데이터베이스 BLOB, CLOB 타입과 매핑
- @Lob에는 지정할 수 있는 속성이 없다.
- 매핑하는 필드 타입이 문자면 CLOB 매핑, 나머지는 BLOB 매핑
  - CLOB: String, char[], java.sql.CLOB 
  - BLOB: byte[], java.sql. BLOB


#### @Transient

```java
@Transient
private Integer temp;
```

- 필드 매핑X
- 데이터베이스에 저장X, 조회X
- 주로 메모리상에서만 임시로 어떤 값을 보관하고 싶을 때 사용

## 기본 키 매핑

```java
@Id @GeneratedValue(strategy = GenerationType.AUTO)
private Long id;
```

**기본 키 매핑 방법**
- 직접 할당: `@Id`만 사용: 내가 값을 직접 할당(이것저것 직접 조합하는 등)하고 싶을 때 사용
- 자동 생성(`@GeneratedValue`)
  - **IDENTITY**: 데이터베이스에 위임, MYSQL 
  - **SEQUENCE**: 데이터베이스 시퀀스 오브젝트 사용, ORACLE
    - `@SequenceGenerator` 필요
  - **TABLE**: 키 생성용 테이블 사용, 모든 DB에서 사용 
    - `@TableGenerator` 필요 
  - **AUTO**: 방언(Dialect)에 따라 자동 지정, 기본값


### IDENTITY 전략 - 특징

```java
@Id @GeneratedValue(strategy = GenerationType.IDENTITY) 
private Long id;
```

```
// H2Dialect
Hibernate: 
    create table Member (
        age integer not null,
        id bigint generated by default as identity,
        name varchar(255),
        primary key (id)
    )
    
    
// MysqlDialect
create table Member (
        age integer not null,
        id bigint not null auto_increment,
        name varchar(255),
        primary key (id)
    ) 
```

- 기본 키 생성을 데이터베이스에 위임
- 주로 MySQL, PostgreSQL, SQL Server, DB2에서 사용 
  - (예: MySQL의 AUTO_INCREMENT)
- JPA는 보통 트랜잭션 커밋 시점에 INSERT SQL 실행
- AUTO_INCREMENT는 데이터베이스에 INSERT SQL을 실행한 이후에 ID 값을 알 수 있음

```java
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
  @Column(name = "name")
  private String username;
  private int age;
}

Member member = new Member();
member.setUsername("A");

System.out.println("============");
em.persist(member);
System.out.println("member.id = " + member.getId());
System.out.println("============");

tx.commit();
```

```
============
Hibernate: 
    /* insert for
        hellojpa.Member */insert 
    into
        Member (age, name, id) 
    values
        (?, ?, default)
member.id = 1
============
```

- ***IDENTITY 전략은 em.persist() 시점에 즉시 INSERT SQL 실행 하고 DB에서 식별자를 조회***
  - IDENTITY 는 DB 에 값이 들어가봐야 PK 값을 알 수 있기 때문에
  - 영속성 컨텍스트에서 관리가 되려면 PK 값을 알아야 한다.
    - 영속 상태(ex: `em.persist()`)를 만들 때, DB 에 바로 insert sql 을 날려서 PK 값을 알아내고 영속성 컨텍스트에서 관리하게 된다.
    - 따라서, 이 전략은 트랜잭션을 지원하는 쓰기 지연은 동작하지 않는다.
    - **member.getId() 를 했을 때, select query 로 가져오지 않는 이유: *jdbc 드라이버(JDBC3)* 에 추가된 `Statement.getGeneratedKeys()` 를 사용하면 데이터를 저장하면서 동시에 생성된 기본키 값도 얻어 올 수 있다.**
    - Hibernate 는 이 메소드를 사용해서 데이터베이스와 한번만 통신한다.


### SEQUENCE 전략 - 특징

```java
@Entity
@SequenceGenerator(
        name = "MEMBER_SEQ_GENERATOR",
        sequenceName = "MEMBER_SEQ", //매핑할 데이터베이스 시퀀스 이름
        initialValue = 1, allocationSize = 1)
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBER_SEQ_GENERATOR")
  private Long id;
}

  Member member = new Member();
member.setUsername("A");

System.out.println("============");
em.persist(member);
System.out.println("member.id = " + member.getId());
System.out.println("============");

tx.commit();
```

```
drop sequence if exists Member_SEQ

create sequence Member_SEQ start with 1 increment by 50

============
Hibernate: 
    select
        next value for MEMBER_SEQ
member.id = 1
============
Hibernate: 
    /* insert for
        hellojpa.Member */insert 
    into
        Member (age, name, id) 
    values
        (?, ?, ?)
```

- create sequence 라고 해서 sequence object 를 만들어낸다.
- **데이터베이스에 있는 Sequence Object 를 통해서 값을 Generate 한다.**
- 데이터베이스 시퀀스는 유일한 값을 순서대로 생성하는 특별한 데이터베이스 오브젝트
  - (예: 오라클 시퀀스)
- 오라클, PostgreSQL, DB2, H2 데이터베이스에서 사용
- Sequence Object 도 데이터베이스가 관리하는 객체이기 때문에, DB 에서 값을 조회할 수 있다.
- `em.persist()` 영속 상태로 만들기 위해서는 PK 값을 알아야 하고, 그렇기 때문에 먼저 Sequence 값을 가져와야 한다.
  - `select next value for MEMBER_SEQ` 그래서 DB 에서 Sequence 값을 조회한 후에 영속성 컨텍스트에 값을 넣어준다.
  - 그래서 `em.persist()` 가 동작할 때는 insert query 가 안날라간다.
  - **그래서 Sequence 전략은 버퍼링 전략(다 모아서 한번에 write 하는 방식)이 가능하다.**
  - *그렇다면, DB 에 계속 select query 를 날려야 하기 때문에 네트워크로 왔다갔다 해야 하는 성능 저하 이슈가 있지 않나?*
    - **allocationSize 로 해결**

#### SEQUENCE - @SequenceGenerator

**주의: allocationSize 기본값 = 50**

![스크린샷 2024-01-31 오후 7.50.53.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_6IRTia%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-31%20%EC%98%A4%ED%9B%84%207.50.53.png)

#### SEQUENCE 전략과 최적화

```java
@Entity
@SequenceGenerator(
        name = "MEMBER_SEQ_GENERATOR",
        sequenceName = "MEMBER_SEQ", //매핑할 데이터베이스 시퀀스 이름
        initialValue = 1, allocationSize = 50)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "MEMBER_SEQ_GENERATOR")
    private Long id;
}
```

- Next call 한번 DB 에 쿼리를 날릴 때, DB 에 미리 50개 사이즈(allocationSize 만큼)를 올려놓고, 메모리에서 1씩 사용하다가 50개를 다 할당하면, 그때 다시 next call 을 날린다.
- 비즈니스 로직의 트랜잭션이 롤백되도, 시퀀스는 롤백 없이 진행되도록 하기 위해 테이블 전략의 경우 별도의 커넥션을 받아서 사용할 정도로 코드가 만들어져 있었습니다.
- 시퀀스는 DB객체로 엔티티 당 별도의 시퀀스 전략을 가져가지 않는다고 한다면 DB에서 생성한 시퀀스를 공유해서 사용한다고 보는게 맞을 것 같습니다.
- MySQL 은 SEQUENCE 전략 을 못쓴다!!!!!! IDENTITY, TABLE 전략만 가능

좋은 질문과 답변들
- [UUID 질문 드립니다.](https://www.inflearn.com/questions/123989/uuid-%EC%A7%88%EB%AC%B8-%EB%93%9C%EB%A6%BD%EB%8B%88%EB%8B%A4)
- [SEQUENCE 전략 초기값과 호출 횟수 문의드립니다.](https://www.inflearn.com/questions/643827)
- [시퀀스 전략에서의 allocationSize에 대해](https://www.inflearn.com/questions/122551)
- [시퀀스 방식에서 롤백시 시퀀스 문의](https://www.inflearn.com/questions/17504)
- [sequence방식 allocationSize 관련 궁금합니다.](https://www.inflearn.com/questions/730439)
- [GenerationType.SEQUENCE 전략](https://www.inflearn.com/questions/598275)
- [기본키 전략 max + 1 문의](https://www.inflearn.com/questions/228082)


### TABLE 전략

```java
@Entity
@TableGenerator(
        name = "MEMBER_SEQ_GENERATOR",
        table = "MY_SEQUENCES",
        pkColumnValue = "MEMBER_SEQ", allocationSize = 1)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "MEMBER_SEQ_GENERATOR")
    private Long id;
```

```
Hibernate: 
    create table MY_SEQUENCES (
        next_val bigint,
        sequence_name varchar(255) not null,
        primary key (sequence_name)
    )
Hibernate: 
    insert into MY_SEQUENCES(sequence_name, next_val) values ('MEMBER_SEQ',0)
```

![스크린샷 2024-01-31 오후 8.00.38.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_LnJ83Q%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-01-31%20%EC%98%A4%ED%9B%84%208.00.38.png)

- `initialValue, allocationSize` 로 최적화한다.

**키 생성 전용 테이블을 하나 만들어서 데이터베이스 시퀀스를 흉내내는 전략**
- 장점: 모든 데이터베이스에 적용 가능 
- 단점: 성능
- 잘 쓰이진 않는다.

[채번 테이블 성능](http://www.gurubee.net/lecture/4253)

### 권장하는 식별자 전략
- **기본 키 제약 조건**: null 아님, 유일, **변하면 안된다.** 
- 미래까지 이 조건을 만족하는 자연키는 찾기 어렵다. 대리키(대체키)를 사용하자.
- 예를 들어 주민등록번호도 기본 키로 적절하기 않다. 
- **권장: Long형 + 대체키(SEQUENCE or UUID) + 키 생성전략(GENERATIONTYPE.IDENTITY 등) 사용**

## 요구사항 분석과 매핑

요구사항 분석
- 회원은 상품을 주문할 수 있다.
- 주문 시 여러 종류의 상품을 선택할 수 있다.

![스크린샷 2024-02-01 오후 8.30.23.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_8VIVfH%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-01%20%EC%98%A4%ED%9B%84%208.30.23.png)

도메인 모델 분석
- **회원과 주문의 관계**: **회원**은 여러 번 **주문**할 수 있다. (일대다)(1:N)
- **주문과 상품의 관계**: **주문**할 때 여러 **상품**을 선택할 수 있다. 반대로 같은 **상품**도 여러 번 **주문**될 수 있다. 
  - **주문상품** 이라는 모델을 만들어서 다대다 관계(N:M)를 일대다(1:N), 다대일(N:1) 관계로 풀어냄

![스크린샷 2024-02-01 오후 8.35.38.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_JB4vZV%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-01%20%EC%98%A4%ED%9B%84%208.35.38.png)

테이블 설계

![스크린샷 2024-02-01 오후 8.36.37.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_P4jAF3%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-01%20%EC%98%A4%ED%9B%84%208.36.37.png)

엔티티 설계와 매핑

![스크린샷 2024-02-01 오후 8.43.45.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_rCQIog%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-01%20%EC%98%A4%ED%9B%84%208.43.45.png)

**데이터 중심 설계의 문제점**
- 현재 방식은 객체 설계를 테이블 설계에 맞춘 방식 
- 테이블의 외래키를 객체에 그대로 가져옴 
- 객체 그래프 탐색이 불가능 
- 참조가 없으므로 UML도 잘못됨

# 4. 다양한 연관관계 매핑

객체가 지향하는 패러다임과 관계형 DB 가 지향하는 패러다임의 불일치로 헷갈릴 수 있으니 주의해야 한다.

## 목표
- **객체와 테이블 연관관계의 차이를 이해** 
- **객체의 참조와 테이블의 외래 키를 매핑**

용어 이해
- **방향(Direction)**: 단방향, 양방향
- **다중성(Multiplicity)**: 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대다(N:M) 이해
- **연관관계의 주인(Owner)**: 객체 양방향 연관관계는 관리 주인이 필요

## 연관관계가 필요한 이유

**객체지향 설계의 목표는 자율적인 객체들의 협력 공동체를 만드는 것이다. –조영호(객체지향의 사실과 오해)**

**예제 시나리오**
- 회원과 팀이 있다.
- 회원과 팀은 다대일 관계다.
- 회원은 하나의 팀에만 소속될 수 있다.

### 객체를 테이블에 맞추어 모델링 (연관관계가 없는 객체)

![스크린샷 2024-02-02 오후 5.52.34.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_4jgEfQ%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-02%20%EC%98%A4%ED%9B%84%205.52.34.png)

```java
@Entity
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  
  @Column(name = "USERNAME")
  private String name;

  @Column(name = "TEAM_ID")
  private Long teamId;

  public Member() {
  }
}

@Entity
public class Team {
  @Id @GeneratedValue
  private Long id;

  private String name;
}

//팀 저장
Team team = new Team();
team.setName("TeamA"); 
em.persist(team);

//회원 저장
Member member = new Member();
member.setName("member1"); 
member.setTeamId(team.getId()); 
em.persist(member);

Member findMember = em.find(Member.class, member.getId());

Long findTeamId = findMember.getTeamId();
Team team = em.find(Team.class, findTeamId);
```

- 외래 키 식별자를 직접 다룸
- **식별자로 다시 조회, 객체 지향적인 방법은 아니다.**

객체를 테이블에 맞추어 데이터 중심으로 모델링하면, **협력 관계를 만들 수 없다.** 
- **테이블은 외래 키로 조인**을 사용해서 연관된 테이블을 찾는다. 
- **객체는 참조**를 이용해서 연관된 객체를 찾는다.
- 테이블과 객체 사이에는 이런 큰 간격이 있다.

## 단방향 연관관계

### 객체 지향 모델링

**객체 연관관계 사용**

![스크린샷 2024-02-03 오후 3.35.14.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_jBjXCL%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-03%20%EC%98%A4%ED%9B%84%203.35.14.png)

**객체의 참조와 테이블의 외래 키를 매핑**

```java
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "USERNAME")
    private String name;

//    @Column(name = "TEAM_ID")
//    private Long teamId;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;

    public Member() {
    }
}
```

**ORM 매핑**

![스크린샷 2024-02-03 오후 3.37.09.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_QB94Ja%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-03%20%EC%98%A4%ED%9B%84%203.37.09.png)

**연관관계 저장**

```java
//팀 저장
Team team = new Team();
team.setName("TeamA");
em.persist(team);

//회원 저장
Member member = new Member();
member.setName("member1");
member.setTeam(team); // 단방형 연관관계 설정, 참조 저장
em.persist(member);
```

**참조로 연관관계 조회 - 객체 그래프 탐색**

```java
//조회
Member findMember = em.find(Member.class, member.getId());

//참조를 사용해서 연관관계 조회
Team findTeam = findMember.getTeam();
```

```
Hibernate: 
    select
        m1_0.id,
        m1_0.USERNAME,
        t1_0.id,
        t1_0.name 
    from
        Member m1_0 
    left join
        Team t1_0 
            on t1_0.id=m1_0.TEAM_ID 
    where
        m1_0.id=?
```

**연관관계 수정**

```java
 // 새로운 팀B
Team teamB = new Team();
teamB.setName("TeamB"); 
em.persist(teamB);

// 회원1에 새로운 팀B 설정
member.setTeam(teamB);
```

## 양방향 연관관계와 연관관계의 주인

### 양방향 매핑

**예시**
![스크린샷 2024-02-03 오후 4.04.28.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_HZlmWs%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-03%20%EC%98%A4%ED%9B%84%204.04.28.png)

***테이블 연관관계에서는 외래키 하나로 양방향이 다 있는 것이다!!!!***
- ***테이블의 연관계에는 방향이라는 것이 없다.*** 

객체에서는 둘 다 세팅을 해줘야 한다.
- **Member 엔티티는 단방향과 동일**
- **Team 엔티티는 컬렉션 추가**

```java
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "USERNAME")
    private String name;

//    @Column(name = "TEAM_ID")
//    private Long teamId;

    @ManyToOne
    @JoinColumn(name = "TEAM_ID")
    private Team team;
}

@Entity
public class Team {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
}
```

`@OneToMany(mappedBy = "team")` 에서의 team 은 Member class 의 team 변수명과 같다.
- 반대편 사이드에 어떤게 있는지 알려주는 것

**반대 방향으로 객체 그래프 탐색**

```java
// 조회
Team findTeam = em.find(Team.class, team.getId());
int memberSize = team.getMembers().size(); // 역방향 조회
```

#### 연관관계의 주인과 mappedBy

- mappedBy = JPA의 멘탈붕괴 난이도
- mappedBy 는 처음에는 이해하기 어렵다.
- 객체와 테이블간에 연관관계를 맺는 차이를 이해해야 한다.
- **테이블 연관관계**에서는 Foreign Key(외래키) 하나로 양쪽 연관관계를 다 가질 수 있다.
- **객체 연관관계**에서는 참조가 양쪽에 다 있어야 한다.

#### 객체와 테이블이 관계를 맺는 차이

**객체 연관관계 = 2개**
- 회원 -> 팀 연관관계 1개(단방향)
- 팀 -> 회원 연관관계 1개(단방향)

**테이블 연관관계 = 1개** 
- 회원 <-> 팀의 연관관계 1개(양방향)

![스크린샷 2024-02-03 오후 4.12.37.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_LOTTYO%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-03%20%EC%98%A4%ED%9B%84%204.12.37.png)

#### 객체의 양방향 관계

- 객체의 **양방향 관계는 사실 양방향 관계가 아니라 서로 다른 단뱡향 관계 2개다.**
- 객체를 양방향으로 참조하려면 **단방향 연관관계를 2개** 만들어야 한다.
  - A -> B (a.getB()) 
  - B -> A (b.getA())

```java
class A { B b; }
class B { A a; }
```

#### 테이블의 양방향 연관관계

- 테이블은 **외래 키 하나**로 두 테이블의 연관관계를 관리
- MEMBER.TEAM_ID 외래 키 하나로 양방향 연관관계 가짐
- **(양쪽으로 조인할 수 있다.)**

```sql
SELECT *
FROM MEMBER M
JOIN TEAM T ON M.TEAM_ID = T.TEAM_ID

SELECT *
FROM TEAM T
JOIN MEMBER M ON T.TEAM_ID = M.TEAM_ID
```

#### 둘 중 하나로 외래 키를 관리해야 한다.

![스크린샷 2024-02-03 오후 4.18.09.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_whIM0i%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-03%20%EC%98%A4%ED%9B%84%204.18.09.png)

객체 연관관계에서 2가지 중 어느 것으로 mapping 을 해야할까?
- Member 에서 Team 으로 가는 참조값이랑 Team 에서 Member 으로 가는 참조값이 있다.
- Member 의 team 값을 update했을 때, 이 외래키 값이 update되어야 할까?
- Team 의 members 값을 update했을 때, 이 외래키 값이 update되어야 할까?
- DB (테이블) 입장에서는 Member 테이블의 TEAM_ID (외래키) 값만 update 되면 된다.

## 연관관계의 주인(Owner)

**양방향 매핑 규칙**
- 객체의 두 관계중 하나를 연관관계의 주인으로 지정
- **연관관계의 주인만이 외래 키를 관리(등록, 수정)**
- **주인이 아닌쪽은 읽기만 가능**
- 주인은 mappedBy 속성 사용 X
- 주인이 아니면 mappedBy 속성으로 주인 지정

### 누구를 주인으로? - 외래 키가 있는 있는 곳을 주인
- ***외래 키가 있는 있는 곳을 주인***으로 정해라!!!!!!!!!!!!!! 
- DB 입장에서 보면, 외래키가 있는 곳이 무조건 N(다) 이고, 외래키가 없는 곳이 1 이다. 1:N 이 되는 것이다.
- 다 쪽이 무조건 연관관계의 주인이 되는 것!!
- 비즈니스 로직 적으로 중요하다기 보다는 그냥 정말 단순히 DB 의 관점에서 N 이 되는 쪽이 그냥 다 쪽이고, 즉, 연관관계의 주인이 되면 된다.
- 외래키 관리가 엔티티와 테이블이 Mapping 된 테이블에서 전부 관리가 되는 것. 성능 이슈도 없다.
- 여기서는 `Member.team`이 연관관계의 주인
  - 생각해보면 이상하다. `Team.members` 값을 update했는데, Member 테이블이 update query 가 날라가면 이상한 것. 
  - 성능이슈도 있다.

![스크린샷 2024-02-03 오후 5.24.03.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_jmRxdK%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-03%20%EC%98%A4%ED%9B%84%205.24.03.png)

#### 양방향 매핑시 가장 많이 하는 실수 (연관관계의 주인에 값을 입력하지 않음)

```java
Team team = new Team();
team.setName("TeamA"); 
em.persist(team);

Member member = new Member();
member.setName("member1");

//역방향(주인이 아닌 방향)만 연관관계 설정
team.getMembers().add(member);
em.persist(member);
```

![스크린샷 2024-02-03 오후 5.24.44.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_JaAQYO%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-03%20%EC%98%A4%ED%9B%84%205.24.44.png)


#### 양방향 매핑시 연관관계의 주인에 값을 입력해야 한다. (순수한 객체 관계를 고려하면 항상 양쪽 다 값을 입력해야 한다.)

***양방향 매핑시에는 양쪽에 값을 다 넣어주는게 사실 맞다!!!!!***
- JPA 의 관점에서는 연관관계의 주인 쪽에서만 값을 넣어주면 되지만,
- 객체지향적인 관점에서는 양쪽에 값을 다 넣어줘야 한다.

```java
Team team = new Team(); 
team.setName("TeamA"); 
em.persist(team);

Member member = new Member(); 
member.setName("member1");

team.getMembers().add(member); 
//연관관계의 주인에 값 설정
member.setTeam(team); 

em.persist(member);
```

![스크린샷 2024-02-03 오후 5.25.18.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_c9WQBH%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-03%20%EC%98%A4%ED%9B%84%205.25.18.png)

**1. 객체지향적인 관점**
- `team.getMembers().add(member);`: 역방향에 값을 안넣어줄 시에
- `em.flush(); em.clear();` 를 안할시에, team, member 둘 다 영속성 컨텍스트 안의 1차 캐시에만 값이 있는 상태이기 때문에,
- List<Member> 컬렉션은 빈 값인 상태이다.

**2. 테스트 케이스를 작성할 때**
- 테스트 케이스는 JPA 가 없는 환경에서도 동작해야 한다.
- 한쪽만 설정할 경우, 다른 한쪽에서 조회할 시에 값이 `null` 일 수 있다.

***따라서, 양방향 매핑시에는 양쪽에 값을 넣어주는게 맞다!!!***

#### 양방향 연관관계 주의 - 실습
- **순수 객체 상태를 고려해서 항상 양쪽에 값을 설정하자**
- *연관관계 편의 메소드*를 생성하자
- 양방향 매핑시에 무한 루프를 조심하자 
  - 예: toString(), lombok, JSON 생성 라이브러리

*연관관계 편의 메소드 예시*
- ***JPA 상태를 변경하는 메서드는 가급적 setXXX 보다는 다른 의미 있는 이름으로 변경하자!!!!!***
```java
public class Member {
    public void changeTeam(Team team) {
        this.team = team;
        team.getMemers().add(this);
    }
}
```

- ***원칙적으로 구 list에 있는 team을 제거하도록 코드를 작성하는 것이 맞습니다. 다만 이 관계에서 list는 연관관계의 주인이 아니므로 실제 DB 에 영향을 주지는 않습니다. 객체까지 고려하면 list에 있는 team을 제거하는 것이 맞지만, 실용적인 관점에서 그냥 두어도 DB에서 삭제되지 않으므로 크게 상관은 없습니다.***
- **삭제하는 로직까지 추가한다면, setTeam(), changeTeam() 메서드는 따로 구현하는게 바람직하다.**

#### 양방향 매핑 정리
- ***단방향 매핑만으로도 이미 연관관계 매핑은 완료***
- 실무에서도 설계를 할 때, 단방향 매핑으로 설계를 끝내야 한다.
- 양방향 매핑은 반대 방향으로 조회(객체 그래프 탐색) 기능이 추가된 것 뿐
- JPQL에서 역방향으로 탐색할 일이 많음
- 단방향 매핑을 잘 하고 양방향은 필요할 때 추가해도 됨 (테이블에 영향을 주지 않음)

#### 연관관계의 주인을 정하는 기준
- 비즈니스 로직을 기준으로 연관관계의 주인을 선택하면 안됨
- **연관관계의 주인은 외래키의 위치를 기준으로 정해야함**

## 실전 예제 - 2. 연관관계 매핑 시작

**테이블 구조**
- 테이블 구조는 이전과 같다.

![스크린샷 2024-02-03 오후 11.25.42.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_Dw9Gh7%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-03%20%EC%98%A4%ED%9B%84%2011.25.42.png)

**객체 구조**
- 참조를 사용하도록 변경

![스크린샷 2024-02-03 오후 11.26.12.png](..%2F..%2F..%2F..%2F..%2Fvar%2Ffolders%2Fh6%2Fl7c1dk657xz0xzltws65m3fh0000gn%2FT%2FTemporaryItems%2FNSIRD_screencaptureui_tKJtfi%2F%EC%8A%A4%ED%81%AC%EB%A6%B0%EC%83%B7%202024-02-03%20%EC%98%A4%ED%9B%84%2011.26.12.png)


 