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

<img width="1287" alt="스크린샷 2024-02-10 오후 9 25 17" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/4403fc07-ce2e-4068-97ce-be166f4a11f1">

JPA 구동 방식
<img width="1361" alt="스크린샷 2024-02-10 오후 9 31 44" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/7659d99e-60a4-49ed-aa7f-3e0f575a849c">

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

//em.persist(member5);

tx.commit();
```

<img width="835" alt="스크린샷 2024-02-10 오후 9 45 14" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/2b8f0b80-1f3f-447d-8cb6-66d2580dcd4c">

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

<img width="669" alt="스크린샷 2024-02-10 오후 9 51 28" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/8949e528-bf64-4837-8fcb-72b55edccb3a">

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

<img width="669" alt="스크린샷 2024-02-10 오후 9 58 02" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/1c9636c0-2923-4da9-881e-a475158c5c7a">

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

<img width="666" alt="스크린샷 2024-02-10 오후 9 59 57" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/1089325d-0c0c-4916-ae97-ec82294fca08">
- `initialValue, allocationSize` 로 최적화한다.

**키 생성 전용 테이블을 하나 만들어서 데이터베이스 시퀀스를 흉내내는 전략**
- 장점: 모든 데이터베이스에 적용 가능 
- 단점: 성능
- 잘 쓰이진 않는다.

[채번 테이블 성능에 관한 글](http://www.gurubee.net/lecture/4253)

### 권장하는 식별자 전략
- **기본 키 제약 조건**: null 아님, 유일, **변하면 안된다.** 
- 미래까지 이 조건을 만족하는 자연키는 찾기 어렵다. 대리키(대체키)를 사용하자.
- 예를 들어 주민등록번호도 기본 키로 적절하기 않다. 
- **권장: Long형 + 대체키(SEQUENCE or UUID) + 키 생성전략(GENERATIONTYPE.IDENTITY 등) 사용**

## 요구사항 분석과 매핑

요구사항 분석
- 회원은 상품을 주문할 수 있다.
- 주문 시 여러 종류의 상품을 선택할 수 있다.

<img width="683" alt="스크린샷 2024-02-10 오후 10 01 31" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/c18a0d94-1a8e-41fe-82c0-74a7ee059d1d">

도메인 모델 분석
- **회원과 주문의 관계**: **회원**은 여러 번 **주문**할 수 있다. (일대다)(1:N)
- **주문과 상품의 관계**: **주문**할 때 여러 **상품**을 선택할 수 있다. 반대로 같은 **상품**도 여러 번 **주문**될 수 있다. 
  - **주문상품** 이라는 모델을 만들어서 다대다 관계(N:M)를 일대다(1:N), 다대일(N:1) 관계로 풀어냄

<img width="581" alt="스크린샷 2024-02-10 오후 10 01 47" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/a20b309b-c211-4df0-8707-dd5acfcfeb59">

**테이블 설계**

<img width="590" alt="스크린샷 2024-02-10 오후 10 02 07" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/a4152836-32b2-4714-9df8-7e1afbe7c64b">

**엔티티 설계와 매핑**

<img width="628" alt="스크린샷 2024-02-10 오후 10 10 14" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/a0a39dfd-363e-4f95-a995-84ce0abec08d">

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

<img width="799" alt="스크린샷 2024-02-10 오후 10 12 26" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/57af7c32-bd16-4d3c-ad8a-5c155b2fc0de">

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

<img width="849" alt="스크린샷 2024-02-10 오후 10 12 55" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/d5f3c589-39ee-41fd-8518-dccb34b08823">

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

<img width="853" alt="스크린샷 2024-02-10 오후 10 13 33" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/bc374454-ea30-42bb-9699-e627d1120861">

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

<img width="809" alt="스크린샷 2024-02-10 오후 10 14 13" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/aef7a9e7-f140-4bdc-ab03-5c24f8ea9b45">

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

<img width="806" alt="스크린샷 2024-02-10 오후 10 14 48" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/c20bddc0-1205-4c17-be00-bc8ca6810b1a">

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

<img width="822" alt="스크린샷 2024-02-10 오후 10 15 19" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/e906d69d-8cbe-4beb-bdb1-9a8e6b178ba1">

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

<img width="713" alt="스크린샷 2024-02-10 오후 10 28 46" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/f3591510-eda8-4e08-b8f0-9080ac4c1190">

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

<img width="540" alt="스크린샷 2024-02-10 오후 10 29 13" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/00d3982a-52f8-4966-bdf0-7e8af33de33c">

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

<img width="539" alt="스크린샷 2024-02-10 오후 10 29 23" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/9a152547-a998-42a9-a450-86fae861525b">

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

<img width="778" alt="스크린샷 2024-02-10 오후 10 30 09" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/95ef43ac-e93d-4898-8678-388fbf90d50b">

**객체 구조**
- 참조를 사용하도록 변경

<img width="859" alt="스크린샷 2024-02-10 오후 10 30 31" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/bbdadd99-86c2-435b-9198-a00415327a9b">

---

# 다양한 연관관계 매핑

## 연관관게 매핑 고려사항 3가지

- 다중성
- 단방향, 양방향
- 연관관계의 주인

### 다중성
- 다대일: `@ManyToOne`
- 일대다: `@OneToMany`
- 일대일: `@OneToOne`
- 다대다: `@ManyToMany` : ***사실 실무에서 쓰면 안된다!!!!!***
- JPA 에서 제공하는 어노테이션들은 전부 DB 와 매핑하기 위해 제공하는 것들이다.
- DB 관점에서의 다중성을 기준으로 고민하면 된다.
- **대칭성**이 있다.

### 단방향, 양방향
- **테이블**
  - **외래 키 하나로 양쪽 조인 가능**
  - 사실 방향이라는 개념이 없음
- **객체**
  - 참조용 필드가 있는 쪽으로만 참조 가능
  - 한쪽만 참조하면 단방향
  - 양쪽이 서로 참조하면 양방향
    - (사실 양방향도 단방향이 2개인 것이다. 양방향은 설명하기 편하기 위해서 만든 개념이다.)

### 연관관계의 주인
- 테이블은 **외래 키 하나**로 두 테이블이 연관관계를 맺음
- 객체 양방향 관계는 A->B, B->A 처럼 **참조가 2군데**
- 객체 양방향 관계는 참조가 2군데 있음. 둘중 테이블의 외래 키를 관리할 곳을 지정해야함
- 연관관계의 주인: 외래 키를 관리하는 참조
- 주인의 반대편: 외래 키에 영향을 주지 않음, 단순 조회만 가능

## 다대일 [N:1]

### 다대일 단방향

<img width="847" alt="스크린샷 2024-02-10 오후 10 38 51" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/4323b44a-1e2e-4e6b-8fae-dc386ab34042">

- DB 설계를 할 때, 테이블의 연관관계가 1:N 이라고 할 때, 항상 N 쪽의 테이블에 외래키(FK) 가 있어야 한다. 안그러면 설계가 잘못된 것이다.
  - EX) Member, Team 테이블이 있고, Team 테이블에 memberId 를 외래키로 설정했을 때, 
  - 같은 Team 에 속한 Member 를 표현하기 위해 memberId 만 다른 데이터를 여러개 넣어야 하는 상황이 온다. 
  - 즉, 설계가 잘못된 것
  - ***따라서, 관계형 DB 에서는 N 쪽에 항상 외래키(FK) 를 설정해줘야 한다.***
- ***외래키가 있는 곳에 연관된 참조(객체)를 넣어주고 연관관계 매핑을 걸어주면 된다.***

**다대일 단방향 정리**
- 가장 많이 사용하는 연관관계
- **다대일**의 반대는 **일대다**

### 다대일 양방향

**다대일 양방향 예시**
<img width="851" alt="스크린샷 2024-02-10 오후 10 39 15" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/1bc14100-2ff6-4332-9fd2-b76f9998343d">

**다대일 양방향 정리**
- 외래 키가 있는 쪽이 연관관계의 주인
- 양쪽을 서로 참조하도록 개발

## 일대다 [1:N]

### 일대다 단방형 예시

- 실무에서 절대 권장하지 않는 방식
- 그러나 있을 수 있는 상황이긴 하다.
- 표준 스펙에서도 지원하긴 하다.

<img width="860" alt="스크린샷 2024-02-10 오후 10 39 41" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/725ba586-3c9b-4a99-a6b2-7ca0d11aa9d4">

- DB 설계상 N 쪽에 외래키(FK) 가 들어갈 수 밖에 없다.
- 이 상황에선 `Team.members` 가 연관관계의 주인이 되는 것이다.
- **그래서, `Team.members` 를 insert, update 할 시에 Member 테이블의 `TEAM_ID` 도 변경해줘야 한다.**

### 일대다 단방향 정리

```java
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String name;
}

@Entity
public class Team {
    @Id
    @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    private String name;

    @OneToMany
    @JoinColumn(name = "TEAM_ID")
    private List<Member> members = new ArrayList<>();
}

Member member = new Member();
member.setName("member1");

em.persist(member);

Team team = new Team();
team.setName("team1");
//
team.getMembers().add(member);

em.persist(team);

tx.commit();
```

```
Hibernate: 
    /* insert for
        hellojpa.Member */insert 
    into
        Member (USERNAME, MEMBER_ID) 
    values
        (?, ?)
Hibernate: 
    /* insert for
        hellojpa.Team */insert 
    into
        Team (name, TEAM_ID) 
    values
        (?, ?)
Hibernate: 
    update
        Member 
    set
        TEAM_ID=? 
    where
        MEMBER_ID=?
```

member table 의 TEAM_ID 를 위해서 update query 가 한번 더 날라가야 한다.
- 운영에서 실제로 잘 안쓰는 이유는 Team entity 에 손을 댔는데, 왜 Member table 에 뭔가 update 가 되지?
- 그리고 실제 운영에서는 수십개의 테이블이 엮여서 돌아가고 있기 때문에 분명히 헷갈릴 가능성이 있다.
- 그렇게 되면, 운영이 되게 힘들어진다.


- 일대다 단방향은 일대다(1:N)에서 **일(1)이 연관관계의 주인**
- 테이블 일대다 관계는 항상 **다(N) 쪽에 외래 키가 있음**
- 객체와 테이블의 차이 때문에 반대편 테이블의 외래 키를 관리하는 특이한 구조
- `@JoinColumn`을 꼭 사용해야 함. 그렇지 않으면 조인 테이블 방식을 사용함(중간에 테이블을 하나 추가함)

### 일대다 단방향 매핑의 단점
- **엔티티가 관리하는 외래 키가 다른 테이블에 있음**
- 연관관계 관리를 위해 추가로 UPDATE SQL 실행
- 일대다 단방향 매핑보다는 **다대일 양방향 매핑**을 사용하자
  - 다대일 단방향이 조금 객체적으로 손해(?)를 볼 수 있더라도, 이 방법이 더 깔끔하다.
    - 손해를 본다는 얘기는 Member 에서 Team 에 대한 참조가 필요없을 수 있는데도, Team 참조를 갖고 있어야 한다.
- 결론적으로, 다대일 단방향, 양방향을 권장하고 그것만 사용한다면, 일대다 단방향은 몰라도 된다!!!!!!

## 일대다 양방향

<img width="853" alt="스크린샷 2024-02-10 오후 10 40 51" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/bac06e4b-0752-4e42-a67b-4f403aee8e37">

```java
@Entity
public class Team {
    @Id
    @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    @OneToMany
    @JoinColumn(name = "TEAM_ID")
    private List<Member> members = new ArrayList<>();
}

public class Member {
    @ManyToOne
    @JoinColumn(name = "TEAM_ID", insertable = false, updatable = false)
    private Team team;
}
```

***읽기 전용 매핑이다!!!!!***
- 결과적으로 `Team.members` 가 연관과계의 주인 역할을 계속하게 된다.
- `Member.team` 도 연관관계의 주인처럼 만들었지만, 읽기 전용으로 (insertable = false, updatable = false) 만들어버린 것이다.
- 결국, 양방향 매핑처럼 만들어버린 것이다.

### 일대다 양방향 정리

- 이런 매핑은 공식적으로 존재 X
- `@JoinColumn(insertable=false, updatable=false)`
- **읽기 전용 필드**를 사용해서 양방향 처럼 사용하는 방법
- **다대일 양방향을 사용하자**

## 일대일 [1:1]

일대일 관계
- **일대일** 관계는 그 반대도 **일대일**
- 주 테이블이나 대상 테이블 중에 외래 키 선택 가능
- 대칭적인 것
  - 주 테이블에 외래 키
  - 대상 테이블에 외래 키
- 외래 키에 데이터베이스 유니크(UNI) 제약조건 추가

### 일대일: 주 테이블에 외래 키 단방향

<img width="852" alt="스크린샷 2024-02-10 오후 10 46 17" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/34dc737b-2823-4f96-9d12-68df7de03988">

```java
@Entity
public class Locker {
    @Id @GeneratedValue
    @Column(name = "LOCKER_ID")
    private Long id;

    private String name;
}

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "MEMBER_ID")
    private Long id;

    @OneToOne
    @JoinColumn(name = "LOCKER_ID")
    private Locker locker;
}
```

**일대일: 주 테이블에 외래 키 단방향 정리**
- 다대일(@ManyToOne) 단방향 매핑과 유사

### 일대일: 주 테이블에 외래 키 양방향

<img width="870" alt="스크린샷 2024-02-10 오후 10 48 08" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/0245c8fc-cca9-4a30-ab5e-7d4821679897">

```java
@Entity
public class Locker {
    @Id @GeneratedValue
    @Column(name = "LOCKER_ID")
    private Long id;

    private String name;
    
    @OneToOne(mappedBy = "locker")
    private Member member;
}

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "MEMBER_ID")
    private Long id;

    @OneToOne
    @JoinColumn(name = "LOCKER_ID")
    private Locker locker;
}
```

**일대일: 주 테이블에 외래 키 양방향 정리**
- 다대일 양방향 매핑 처럼 **외래 키가 있는 곳이 연관관계의 주인**
- 반대편은 `mappedBy` 적용

### 일대일: 대상 테이블에 외래 키 단방향

<img width="873" alt="스크린샷 2024-02-10 오후 10 48 25" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/8bafa4f5-8473-4f29-b646-06c499a2010b">

**일대일: 대상 테이블에 외래 키 단방향 정리**
- 단방향 관계는 JPA 지원X
- 양방향 관계는 지원

### 일대일: 대상 테이블에 외래 키 양방향

<img width="863" alt="스크린샷 2024-02-10 오후 10 48 40" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/0371700d-754c-419c-a323-afe4489cd2fa">

**일대일: 대상 테이블에 외래 키 양방향 정리**
- 사실 일대일 주 테이블에 외래 키 양방향과 매핑 방법은 같음

### 일대일 정리
- ***주 테이블에 외래 키***
  - `예시에서 Member 를 통해서 Locker 를 access`
  - 주 객체가 대상 객체의 참조를 가지는 것 처럼 주 테이블에 외래 키를 두고 대상 테이블을 찾음
  - **객체지향 개발자 선호**
  - JPA 매핑 편리
  - 장점: 주 테이블만 조회해도 대상 테이블에 데이터가 있는지 확인 가능
  - 단점: 값이 없으면 외래 키에 null 허용
- ***대상 테이블에 외래 키***
  - `예시에서 Locker 를 통해서 Member 를 access`
  - 대상 테이블에 외래 키가 존재
  - **전통적인 데이터베이스 개발자 선호**
  - 장점: 주 테이블과 대상 테이블을 일대일에서 일대다 관계로 변경할 때 테이블 구조 유지
    - 예를 들어, Member (1) 가 여러개의 Locker (N) 를 가질 수 있는 비즈니스 로직으로 변경될 수 있다.
    - 먼 미래를 생각했을 때
  - 단점: 프록시 기능의 한계로 **지연 로딩으로 설정해도 항상 즉시 로딩됨**(프록시는 뒤에서 설명)
    - 어차피 Member 에 locker 가 있는지 조회하려면 member 를 조회하고, 또 locker 를 조회하는 select query (where locker.member_id = ?) 를 한번 더 날려야 한다. 
    - 총 2번의 select query 를 날려야 한다.

![OneToOne  eager loading example](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/71fdeece-ad2d-4bc2-a4cf-0f29c30f9202)

> 왜 1:1 일때만 프록시 기능의 한계로 지연 로딩으로 설정해도 항상 즉시 로딩됨 << 이런 현상이 나타는건가요 ? [일대다 양방향 일떈 lazy 확인.]

> 이 내용을 이해하려면 값이 없을 때 어떻게 되는가를 이해해야 합니다.
> 예를 들어서 A,B 엔티티가 다음과 같이 설정이 되어 있습니다.
> 
> 외래키는 B에 있고, B가 연관관계의 주인입니다.
> 
> A {
> 
> @OneToOne B b
> 
> }
> 
> 만약 b의 결과가 없다면 어떻게 노출되어야 할까요?
> 
> 바로 결과는 null이 되어야 합니다.
> 
> 그런데 만약 A -> B가 지연로딩 관계라면 b는 프록시 객체가 되어야 합니다.
> 
> 여기에서 문제가 발생합니다.
> 
> 만약 이 상황에서 프록시를 적용하기 위해서 b가 프록시 객체가 된다면 b는 null이 아니라 프록시 객체를 가지게 됩니다.
> 
> 이런 상황에서 a.b를 호출한다면 우리가 기대하는 결과는 null이어야 하는데, 프록시가 되겠지요?
> 
> 결국 프록시를 미리 넣게되면 null이라는 것을 넣을 수 없는 문제가 발생합니다.
> 
> @OneToOne에서 외래키가 A에 있으면 A를 조회하는 순간 B의 데이터가 있는지 확인할 수 있습니다. 그래서 null을 입력해야 할지, 아니면 프록시를 입력해야 할지 명확한 판단이 가능하지요.
> 
> 그런데 지금 처럼 @OneToOne에서 외래키가 B에 있으면 A를 조회할 때 B가 데이터가 있는지 없는지 판단이 불가능합니다. 그래서 null을 입력할지 아니면 프록시를 입력해야 할지 판단히 불가능합니다. 따라서 이 경우 강제로 즉시 로딩을 해서 데이터가 있으면 해당 데이터를 넣고, 없으면 null을 입력하게 됩니다.
> 
> 자 그러면 컬렉션의 경우에는 어떻게 될까요?
> 
> 컬렉션은 재미있게도 null일 필요가 없습니다. 컬렉션 자체가 데이터가 없는 Empty를 표현할 수 있습니다.
> 
> 컬렉션 타입의 객체에 데이터가 없을 때 null이 아닌, 비어있는 컬렉션(Empty Collection)으로 표현할 수 있다는 뜻입니다.
>
> 예를 들어, List<String> list = new ArrayList<>(); 처럼 선언하고 초기화할 수 있습니다. 이때 list는 데이터가 하나도 없는 비어있는 상태를 가지게 되지만, list 자체는 null이 아닙니다.
> 
> 따라서 컬렉션은 항상 지연로딩으로 동작할 수 있습니다.

# 다대다 [N:M]

## 다대다

<img width="524" alt="스크린샷 2024-02-21 오전 1 15 09" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/1b52f725-3e1f-49d5-8cdd-28d27d6692d3">

- 관계형 데이터베이스는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없음
- **연결 테이블**을 추가해서 일대다, 다대일 관계로 풀어내야함

<img width="585" alt="스크린샷 2024-02-21 오전 1 15 50" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/3cd43af8-937e-4929-a1d4-993e880239e1">

- 객체는 컬렉션을 사용해서 객체 2개로 다대다 관계 가능
- 객체에서는 Member 도 ProductList 를 가질 수 있고, Product 도 MemberList 를 가질 수 있기 때문에
- 관계형 데이터베이스와 객체와 차이가 발생하기 때문에
- ORM 에서는 연결 테이블을 하나 더 만들어서 관계를 풀어나가야 함.

```java
import hellojpa.Member;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;

import java.util.ArrayList;

public class Memner {
  @ManyToMany
  @JoinTable(name = "MEMBER_PRODUCT")
  private List<Product> products = new ArrayList<>();
}

public class Product {
  @ManyToMany(mappedBy = "products")
  private List<Member> members = new ArrayList<>();
}
```

- **@ManyToMany** 사용
- **@JoinTable**로 연결 테이블 지정
- 다대다 매핑: 단방향, 양방향 가능

### 다대다 매핑의 한계

<img width="664" alt="스크린샷 2024-02-21 오전 1 16 35" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/7516f3f9-43d4-48a3-918b-d292aa7ddb8d">

여기서 Member_Product 테이블은 전통적인 방식으로 MEMEBER_ID, PRODUCT_ID 2개의 id 를 묶어서 PK 로 가진다.

MEMBER_ID, PRODUCT_ID 가 각각 PK 이면서, FK 이다.

**그러나 웬만하면 PK 는 의미없는 값인 *@GeneratedValue* 로 값을 세우자!!!! 그러면 유연성이 생긴다!!**

- **편리해 보이지만 실무에서 사용 X**
- 연결 테이블이 단순히 연결만 하고 끝나지 않음
- 주문시간, 수량 같은 데이터가 들어올 수 있음

**그러나, 중간테이블에는 매핑 정보 외에 다른 추가적인 정보들을 추가가 불가능하다!!**

**쿼리도 중간 테이블을 조인해서 쿼리가 날라가기 때문에, 이상하게 날라간다.**

### 다대다 한계 극복

<img width="565" alt="스크린샷 2024-02-21 오전 1 18 52" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/7f140840-48d0-400b-a1d9-5f105c49e906">

```java
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Entity
public class Member {
  @OneToMany(mappedBy = "member")
  private List<MemberProduct> memberProducts = new ArrayList<>();
}

@Entity
public class MemberProduct {
  @Id
  @GeneratedValue
  private Long id;

  @ManyToOne
  @JoinColumn(name = "MEMBER_ID")
  private Member member;

  @ManyToOne
  @JoinColumn(name = "PRODUCT_ID")
  private Product product;

  private int count;
  private int price;

  private LocalDateTime orderDateTime;
}

@Entity
public class Product {
  @OneToMany(mappedBy = "product")
  private List<MemberProduct> memberProducts = new ArrayList<>();
}
```

- **연결 테이블용 엔티티 추가(연결 테이블을 엔티티로 승격)**
- **@ManyToMany -> @OneToMany, @ManyToOne**
- ***일관성 있게 모든 테이블에 *@GeneratedValue* 로 PK 를 깐다!!!!***
- **실제 운영에서는 애플리케이션이 확장 가능성이 많은데, Id 값이 종속적이게 물리게 된다면 제약사항이 많아져서 확장성에 용이하지 않다.**

### @JoinColumn
- **외래 키(FK)를 매핑할 때 사용**

<img width="711" alt="스크린샷 2024-02-22 오전 12 59 09" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/25a48861-4810-417e-a8f3-679a37a44e7a">

### @ManyToOne - 주요 속성
- 다대일 관계 매핑

<img width="705" alt="스크린샷 2024-02-22 오전 12 59 26" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/21e63396-b43b-4884-b8dd-fcf1f0122d34">

### @OneToMany - 주요 속성
- 일대다 관계 매핑

<img width="703" alt="스크린샷 2024-02-22 오전 12 59 57" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/d3e891d9-5e11-454f-9e3a-1e0529cc4fce">

# 고급 매핑

## 상속 관계 매핑

### 상속관계 매핑

![상속관계 매핑](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/91fc918c-55bd-4014-95f0-41e1c0df0395)

- 관계형 데이터베이스는 상속 관계X
- **관계형 DB** 에는 **슈퍼타입 / 서브타입 관계라는 모델링 기법**이 **객체 상속과 유사**
- **상속관계 매핑: 객체의 상속과 구조와 DB의 슈퍼타입 서브타입 관계를 매핑**

상속관계 매핑이라는 것은 DB 의 슈퍼타입, 서브타입 관계라는 이 논리 모델링 기법을 어떤 3가지 방법으로 구현을 하든 다 매핑을 하도록 JPA 가 지원을 해준다.

- 슈퍼타입 서브타입 논리 모델을 실제 물리 모델로 구현하는 방법
  - 각각 테이블로 변환 -> 조인 전략 
  - 통합 테이블로 변환 -> 단일 테이블 전략 
  - 서브타입 테이블로 변환 -> 구현 클래스마다 테이블 전략

```java
@Entity
public abstract class Item {
  @Id @GeneratedValue
  private Long id;

  private String name;
  private int price;
}

@Entity
public class Album extends Item {
  private String artist;
}

@Entity
public class Book extends Item {
  private String author;
  private String isbn;
}

@Entity
public class Movie extends Item {
  private String director;
  private String actor;
}
```

```
Hibernate: 
    create table Item (
        price integer not null,
        id bigint not null,
        DTYPE varchar(31) not null,
        actor varchar(255),
        artist varchar(255),
        author varchar(255),
        director varchar(255),
        isbn varchar(255),
        name varchar(255),
        primary key (id)
    )
```

**이렇게 하면 JPA 의 기본 전력은 단일 테이블 전략으로 생성된다.**

### 주요 어노테이션
- `@Inheritance(strategy=InheritanceType.XXX)` 
  - **JOINED: 조인 전략** 
  - **SINGLE_TABLE: 단일 테이블 전략** 
  - **TABLE_PER_CLASS: 구현 클래스마다 테이블 전략** 
- `@DiscriminatorColumn(name="DTYPE")`
- `@DiscriminatorValue("XXX")`

### 조인 전략

![조인 전략](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/0e62e392-782b-4a82-b715-b4b89201b39f)

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Item {
  @Id @GeneratedValue
  private Long id;

  private String name;
  private int price;
}
```

```
Hibernate: 
    create table Album (
        id bigint not null,
        artist varchar(255),
        primary key (id)
    )
Hibernate: 
    create table Book (
        id bigint not null,
        author varchar(255),
        isbn varchar(255),
        primary key (id)
    )
Hibernate: 
    create table Movie (
        id bigint not null,
        actor varchar(255),
        director varchar(255),
        primary key (id)
    )    
Hibernate: 
    create table Item (
        price integer not null,
        id bigint not null,
        name varchar(255),
        primary key (id)
    )
```

기존과는 다르게 `@Inheritance(strategy = InheritanceType.JOINED)` 을 사용하면, 각각의 테이블이 생성되는 것을 알 수 있다.

```java
public class JpaMain {

  public static void main(String[] args) {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
    EntityManager em = emf.createEntityManager();
    //code

    EntityTransaction tx = em.getTransaction();
    tx.begin();

    try {
      Movie movie = new Movie();
      movie.setName("괴물");
      movie.setActor("송강호");
      movie.setDirector("봉준호");
      movie.setPrice(100000);

      em.persist(movie);

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
Hibernate: 
    /* insert for
        hellojpa.Movie */insert 
    into
        Item (name, price, id) 
    values
        (?, ?, ?)
Hibernate: 
    /* insert for
        hellojpa.Movie */insert 
    into
        Movie (actor, director, id) 
    values
        (?, ?, ?)
```

- db 에 저장하게 되면 item, movie 테이블에 각각 insert query 가 날라간다. (총 2번)

![Item 테이블의 PK 가 Movie 테이블의 PK 이자 FK](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/7a1dc554-7e70-460e-a738-9c660bcf10aa)

> Item 의 PK 인 ID 의 1 값이 자식테이블인 Movie 테이블의 ID 와 값이 1로 동일하다. 왜냐하면, Movie 테이블의 ID 값은 PK 이면서 동시에 FK 이기 때문이다.

```java
em.persist(movie);

em.flush();
em.clear();

Movie findMovie = em.find(Movie.class, movie.getId());
```

```
Hibernate: 
    select
        m1_0.id,
        m1_1.name,
        m1_1.price,
        m1_0.actor,
        m1_0.director 
    from
        Movie m1_0 
    join
        Item m1_1 
            on m1_0.id=m1_1.id 
    where
        m1_0.id=?
```

조회 select 를 하게 되면, item, movie 를 inner join 해서 가져오게 된다. **JPA 가 상속 관계일 때, join 까지 해서 갖고올 수 있게 해준다.**

```java
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorValue;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "ITEM_TYPE")
public abstract class Item {}

@Entity
@DiscriminatorValue("M")
public class Movie extends Item {}
```

상속 관계일 때, 이처럼 DiscriminatorType 을 column 으로 넣어서, 어떤 테이블과 연관관계가 있는지 알 수 있게 해주는 것이 운영상으로도 좋다. 물론, 조인전략에서는 DTYPE 이 없어도 join 을 해서 알 수는 있다.

```java
Item item = em.find(Item.class, movie.getId());
```

```
Hibernate: 
    select
        i1_0.id,
        case 
            when i1_1.id is not null 
                then 1 
            when i1_2.id is not null 
                then 2 
            when i1_3.id is not null 
                then 3 
            end,
            i1_0.name,
            i1_0.price,
            i1_1.artist,
            i1_2.author,
            i1_2.isbn,
            i1_3.actor,
            i1_3.director 
        from
            Item i1_0 
        left join
            Album i1_1 
                on i1_0.id=i1_1.id 
        left join
            Book i1_2 
                on i1_0.id=i1_2.id 
        left join
            Movie i1_3 
                on i1_0.id=i1_3.id 
        where
            i1_0.id=?
```

- 장점 
  - 테이블 정규화 
  - 외래 키 참조 무결성 제약조건 활용가능
  - 저장공간 효율화
- 단점 
  - 조회시 조인을 많이 사용, 성능 저하 
  - 조회 쿼리가 복잡함 
  - 데이터 저장시 INSERT SQL 2번 호출

**기본적으 JOINED 전략이 정석이라고 본다. 객체랑도 잘 맞고, 정규화도 되고, 설계 입장에서도 깔끔하게 나온다.**

### 단일 테이블 전략

![단일 테이블 전략](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/a1822e45-e4a0-484b-b80c-76b28e933d06)

```java
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class Item {}
```

`@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` SINGLE_TABLE 로만 변형하면 된다. 처음에 조인전략으로 가다가 성능이 너무 안나와서 단일 테이블 전략으로 변경시에 코드 변경, 쿼리 변경이 필요 없고 테이블만 새로 생성하고, InheriatnceType 변경만 해주면 된다.

```
Hibernate: 
    create table Item (
        price integer not null,
        id bigint not null,
        ITEM_TYPE varchar(31) not null,
        actor varchar(255),
        artist varchar(255),
        author varchar(255),
        director varchar(255),
        isbn varchar(255),
        name varchar(255),
        primary key (id)
    )
    
Hibernate: 
    /* insert for
        hellojpa.Movie */insert 
    into
        Item (name, price, actor, director, DTYPE, id) 
    values
        (?, ?, ?, ?, 'Movie', ?)
Hibernate: 
    select
        m1_0.id,
        m1_0.name,
        m1_0.price,
        m1_0.actor,
        m1_0.director 
    from
        Item m1_0 
    where
        m1_0.DTYPE='Movie' 
        and m1_0.id=?
```

테이블도 Item 테이블 하나만 생성되고, insert 쿼리도 1번, select 쿼리도 1번만 수행된다. **단, DiscriminatorType(DTYPE) 컬럼이 필수로 생성된다. 이전에는 @DiscriminatorColumn 이 없으면 DTYPE 이 생성이 안되었지만, 지금은 어노테이션을 달지 않아도 자동으로 DTYPE 컬럼이 생성된다.**

- 장점 
  - 조인이 필요 없으므로 일반적으로 조회 성능이 빠름 
  - 조회 쿼리가 단순함
- 단점 
  - **자식 엔티티가 매핑한 컬럼은 모두 null 허용**
  - 단일 테이블에 모든 것을 저장하므로 테이블이 커질 수 있다. 상황에 따라서 조회 성능이 오히려 느려질 수 있다. -> 사실 이정도 되려면 임계점을 넘어야 하는데, 임계점 넘을 일이 잘 없긴 하다.

### 구현 클래스마다 테이블 전략

![구현 클래스마다 테이블 전략](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/d20e8139-ed9a-4c18-99fc-190a0a9ee2b9)

```java
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Item {}
```

```
Hibernate: 
    create table Album (
        price integer not null,
        id bigint not null,
        artist varchar(255),
        name varchar(255),
        primary key (id)
    )
Hibernate: 
    create table Book (
        price integer not null,
        id bigint not null,
        author varchar(255),
        isbn varchar(255),
        name varchar(255),
        primary key (id)
    )
Hibernate: 
    create table Movie (
        price integer not null,
        id bigint not null,
        actor varchar(255),
        director varchar(255),
        name varchar(255),
        primary key (id)
    )
   
    
Hibernate: 
    /* insert for
        hellojpa.Movie */insert 
    into
        Movie (name, price, actor, director, id) 
    values
        (?, ?, ?, ?, ?)
Hibernate: 
    select
        m1_0.id,
        m1_0.name,
        m1_0.price,
        m1_0.actor,
        m1_0.director 
    from
        Movie m1_0 
    where
        m1_0.id=?
```

Item 테이블 없이, Album, Movie, Book 3개의 테이블 생성

그러나, 부모 타입등 다른 방법으로 조회 시에 문제가 된다.

```java
Item item = em.find(Item.class, movie.getId());
```

```
select
        i1_0.id,
        i1_0.clazz_,
        i1_0.name,
        i1_0.price,
        i1_0.artist,
        i1_0.author,
        i1_0.isbn,
        i1_0.actor,
        i1_0.director 
    from
        (select
            price,
            id,
            artist,
            name,
            null as author,
            null as isbn,
            null as actor,
            null as director,
            1 as clazz_ 
        from
            Album 
        union
        all select
            price,
            id,
            null as artist,
            name,
            author,
            isbn,
            null as actor,
            null as director,
            2 as clazz_ 
        from
            Book 
        union
        all select
            price,
            id,
            null as artist,
            name,
            null as author,
            null as isbn,
            actor,
            director,
            3 as clazz_ 
        from
            Movie
    ) i1_0 
where
    i1_0.id=?
```

**union all 을 해서 테이블들을 하나하나 다 찾아보는 아주 복잡한 select 쿼리를 보내게 된다!!!**

- **이 전략은 데이터베이스 설계자와 ORM 전문가 둘 다 추천X**
  - 장점
    - 서브 타입을 명확하게 구분해서 처리할 때 효과적
    - not null 제약조건 사용 가능
  - 단점 
    - 여러 자식 테이블을 함께 조회할 때 성능이 느림(UNION SQL 필요)
    - 자식 테이블을 통합해서 쿼리하기 어려움

단점에 대한 하나의 예시를 들어본다면, album, movie, book 3개의 테이블을 갖고 정산을 한다고 했을 때, 새로운 테이블이 추가가 될 때 마다 정산 코드를 새롭게 짜야 한다. 
- 각각의 테이블마다 정산을 돌려야 하는 상황이 발생할 수 있다.

> 결론적으로, 기본적으로 조인전략을 깔고 가지만, 정말 단순하고 데이터의 양도 얼마 안될 때는 단일 테이블 전략을 선택해도 된다. 둘 다 각자의 장단점이 있기 때문에 trade-off 가 있고, 그에 맞게 상황에 맞게 잘 선택하면 된다.

# @MappedSuperclass

## @MappedSuperclass
공통 매핑 정보가 필요할 때 사용(id, name)

![@MappedSuperclass](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/925e9369-0ded-4e1c-b559-722ddc87a022)

### @MappedSuperclass 예시

```java
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity {
  private String createdBy;
  private LocalDateTime createdAt;
  private String LastModifiedBy;
  private LocalDateTime lastModifiedAt;
}

@Entity
public class Member extends BaseEntity {}

@Entity
public class Team extends BaseEntity {}
```

```
Hibernate: 
    create table Member (
        LOCKER_ID bigint unique,
        MEMBER_ID bigint not null,
        TEAM_ID bigint,
        createdAt timestamp(6),
        lastModifiedAt timestamp(6),
        LastModifiedBy varchar(255),
        USERNAME varchar(255),
        createdBy varchar(255),
        primary key (MEMBER_ID)
    )
    
    Hibernate: 
    create table Team (
        TEAM_ID bigint not null,
        createdAt timestamp(6),
        lastModifiedAt timestamp(6),
        LastModifiedBy varchar(255),
        createdBy varchar(255),
        name varchar(255),
        primary key (TEAM_ID)
    )
```

> createdAt, lastModifiedAt 같은 정보들은 직접 setXXX() 해서 넣는 것이 아닌 전부 자동화할 수 있다. login 되어 있는 session 정보룰 읽어와서 넣어주는 것들을 JPA 의 event(?) 라는 기능으로 할 수 있고, Spring Data JPA 로 넘어가면 Annotation 으로 이것을 더 깔끔하게 처리할 수 있다.

**@MappedSuperclass**
- 상속관계 매핑X
- 엔티티X, 테이블과 매핑X
- 부모 클래스를 상속 받는 **자식 클래스에 매핑 정보만 제공**
- 조회, 검색 불가(**em.find(BaseEntity) 불가**)
- ***직접 생성해서 사용할 일이 없으므로 추상 클래스 권장!!!!!***
- 테이블과 관계 없고, 단순히 엔티티가 공통으로 사용하는 매핑 정보를 모으는 역할
- 주로 등록일, 수정일, 등록자, 수정자 같은 전체 엔티티에서 공통으로 적용하는 정보를 모을 때 사용
- **참고: @Entity 클래스는 엔티티(@Entity)나 @MappedSuperclass로 지정한 클래스만 상속 가능**

> 결론적으로, 처음엔 객체지향적으로 설계를 하고 개발을 하다가, 서비스가 엄청 커지고, 트래픽이 많이 몰리게 된다면 객체를 JSON 으로 말아서 컬럼으로 Item 테이블 같은 곳에 집어넣어도 된다. 그건 정답이 없고 상황에 따라서 맞춰야 한다.

# 프록시와 연관관계 관리

## 프록시

### Member를 조회할 때 Team도 함께 조회해야 할까?

![Member를 조회할 때 Team도 함께 조회해야 할까?](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/a3e47ecf-4468-47a7-8b76-b6f740ba40d7)

**회원과 팀 함께 출력**
```java
public void printUserAndTeam(String memberId) {
    Member member = em.find(Member.class, memberId);
    Team team = member.getTeam();
    System.out.println("회원 이름: " + member.getUsername());
    System.out.println("소속팀: " + team.getName());
}
```

**회원만 출력**
```java
public void printUser(String memberId) {
    Member member = em.find(Member.class, memberId);
    Team team = member.getTeam();
    System.out.println("회원 이름: " + member.getUsername());
}
```

항상 Member 와 Team 을 함께 출력하지 않고, Member 하나만 출력할 수도 있다. 그런데, JPA 입장에서 매번 Member 와 연관관계에 있는 Team 을 같이 가져온다면 이것은 낭비가 될 것이다. ***JPA 는 이러한 문제를 프록시와 지연 로딩으로 해결한다!!!***

### 프록시 기초

![프록시 기초](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/0d487ad8-76fd-4f52-84e2-d36989af1826)

- em.find() vs em.**getReference()**
- em.find(): 데이터베이스를 통해서 실제 엔티티 객체 조회
- em.getReference(): **데이터베이스 조회를 미루는 가짜(프록시) 엔티티 객체 조회**

#### em.getReference() 예제

```java
//          Member findMember = em.find(Member.class, member.getId());
Member findReference = em.getReference(Member.class, member.getId());

System.out.println("==========findReference 호출 X==========");
System.out.println("==========findReference 호출 X==========");

System.out.println("==========findReference 호출 O==========");
System.out.println("findReference.class = " + findReference.getClass());
System.out.println("findReference.id = " + findReference.getId());
System.out.println("findReference.name  = " + findReference.getName());
System.out.println("==========findReference 호출 O==========");
```

```
==========findReference 호출 X==========
==========findReference 호출 X==========
==========findReference 호출 O==========
findReference.class = class hellojpa.Member$HibernateProxy$KKHXanwL
findReference.id = 1
Hibernate: 
    select
       ... 
    from
        Member m1_0 
    left join
        Team t1_0 
            on t1_0.TEAM_ID=m1_0.TEAM_ID 
    where
        m1_0.MEMBER_ID=?
findReference.name  = member1
==========findReference 호출 O==========
```

- `em.find()` 와 달리 `em.getReference()` 만 했을 때는 select 쿼리가 날라가지 않고,
- `em.getReference()` 호출 후에, `findReference.getName()` 을 하면 select 쿼리가 날라가는 것을 볼 수 있다.
- `findReference.getId()` 를 할 때에도, select 쿼리는 호출되지 않는다. `em.getReference(Member.class, member.getId())` 를 했을 때, 메서드에 값을 넣어줬기 때문이다.
- `findReference.class = class hellojpa.Member$HibernateProxy$KKHXanwL`
  - getReference() 를 하면, Hibernate 가 강제로 만든 가짜 클래스라는 얘기, Proxy class

### 프록시 특징

![프록시 특징](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/d0dc635a-d788-40bf-86e8-6af69764dbe7)

- 실제 클래스를 상속 받아서 만들어짐
- 실제 클래스와 겉 모양이 같다.
- 사용하는 입장에서는 진짜 객체인지 프록시 객체인지 구분하지 않고 사용하면 됨(이론상)

![프록시 특징2](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/3314242c-ccd8-45aa-9103-4875bda47ddb)

- 프록시 객체는 실제 객체의 참조(target)를 보관
- 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드 호출

### 프록시 객체의 초기화

![프록시 객체의 초기화](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/6b3b5efa-e77a-467b-9428-fc22ce709d5b)

```java
Member member = em.getReference(Member.class, "id1");
member.getName();
```

**영속성 컨텍스트에 초기화 요청을 하는 것이 중요. `Member target = null` 값을 채우는 것이다.**

### 프록시의 특징 (이게 중요하다!!!!)

```java
Member findReference = em.getReference(Member.class, member.getId());

System.out.println("before findReference = " + findReference.getClass());
System.out.println("findReference.id = " + findReference.getId());
System.out.println("findReference.name  = " + findReference.getName());
System.out.println("after findReference = " + findReference.getClass());
```

```
before findReference = class hellojpa.Member$HibernateProxy$JH7TDBfl
findReference.id = 1
Hibernate: 
    select ...
findReference.name  = member1
after findReference = class hellojpa.Member$HibernateProxy$JH7TDBfl
```

- **프록시 객체는 처음 사용할 때 한 번만 초기화**
- **프록시 객체를 초기화 할 때, 프록시 객체가 실제 엔티티로 바뀌는 것은 아님**, 초기화되면 프록시 객체를 통해서 실제 엔티티에 접근 가능

```java
public static void main(String[] args) {
  Member m1 = em.find(Member.class, member2.getId());
  Member m2 = em.getReference(Member.class, member2.getId());
  equalCompare(m1, m2);
  instanceOfComapre(m1, m2);
}

private static void equalCompare(Member m1, Member m2) {
  System.out.println("m1 == m2: " + (m1.getClass() == m2.getClass()));
}

private static void instanceOfCompare(Member m1, Member m2) {
  System.out.println("m1 == m2: " + (m1 instanceof Member));
  System.out.println("m1 == m2: " + (m2 instanceof Member));
}
```

**프록시 객체는 원본 엔티티를 상속받음, 따라서 *타입 체크시 주의해야함* (== 비교 실패, 대신 *instance of 사용*)**

```java
// member1 객체 생성 및 영속화
em.flush();
em.clear();

Member m = em.find(Member.class, member1.getId());
System.out.println("m.class = " + m.getClass());

Member ref = em.getReference(Member.class, member1.getId());
System.out.println("ref.class = " + ref.getClass());

System.out.println("m == ref: " + (m == ref));
```

```
Hibernate: 
    select ...
m.class = class hellojpa.Member
ref.class = class hellojpa.Member
m == ref: true
```

- 영속성 컨텍스트에 찾는 엔티티가 이미 있으면 em.getReference()를 호출해도 실제 엔티티 반환
  1. **JPA 에서는 _같은_ 영속성 컨텍스트의 1차 컨텍스트 내에서 *PK 가 같은 것*을 가져와서 *== 비교*를 하면 항상 true 를 반환해야 한다. -> repeatable read 보장** 
     - **repeatable read: 한 트랜잭션 내에서 같은 데이터를 반복해서 읽어도 처음에 읽었던 데이터와 동일한 데이터를 보게 됩니다.**
  2. 이미 영속성 컨텍스트의 1차 캐시에 member 객체가 존재하는데, 그것을 Proxy 로 갖고 와봐야 아무 이점이 없다.

```java
// member1 객체 생성 및 영속화
em.flush();
em.clear();

Member refMember = em.getReference(Member.class, member1.getId());
System.out.println("refMember.class = " + refMember.getClass());

Member findMember = em.find(Member.class, member1.getId());
System.out.println("findMember.class = " + findMember.getClass());

System.out.println("m == ref: " + (refMember == findMember));
```
```
refMember.class = class hellojpa.Member$HibernateProxy$VyixtOQK
Hibernate: 
    select ...
findMember.class = class hellojpa.Member$HibernateProxy$VyixtOQK
m == ref: true
```

JPA 에서는 _같은_ 영속성 컨텍스트의 1차 컨텍스트 내에서 *PK 가 같은 것*을 가져와서 *== 비교*를 하면 항상 true 를 반환해야 하기 때문에
- em.reference() 이후에 em.find() 를 했을 때, 영속성 컨텍스트의 동일성을 보장하기 위해 해당 프록시 객체를 그대로 반환하되, **내부에서 프록시를 한번 초기화 해준다.**
- 처음이 Proxy 객체이면, `em.find()` 로 조회해도 Proxy 객체가 나올 수 있다.
- 실제 개발을 할 때, 이 객체가 Proxy 이든 아니든, 문제가 없게 개발하는 것이 중요하다.

```java
Member refMember = em.getReference(Member.class, member1.getId());

em.detach(refMember);
// em.clear();
// em.close(); 3가지 메서드 모두 exception 발생

System.out.println("refMember = " + refMember);
```

```
em.detach(member); em.clear(); 수행시에 exception
org.hibernate.LazyInitializationException: could not initialize proxy [hellojpa.Member#1] - no Session

em.close(); 수행시에 exception
java.lang.IllegalStateException: Session/EntityManager is closed
```

***영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태(detach)일 때, 프록시를 초기화하면 문제 발생 (하이버네이트는 org.hibernate.LazyInitializationException 예외를 터트림)***
- 실무에서 이와 같은 문제가 정말 많이 발생!!!!!
- 트랜잭션이 시작하고 끝날 때, 영속성 컨텍스트도 시작하고 끝이 난다.
  - 트랜잭션이 끝나고 나서 Proxy 를 조회하면 `no Session` 에러가 발생한다.


### 프록시 확인

```java
 Member refMember = em.getReference(Member.class, member1.getId());
System.out.println("refMember.getClass() = " + refMember.getClass()); // Proxy
System.out.println("isLoaded = " + emf.getPersistenceUnitUtil().isLoaded(refMember));
```

- **프록시 인스턴스의 초기화 여부 확인** 
  - `PersistenceUnitUtil.isLoaded(Object entity)`

```java
Member refMember = em.getReference(Member.class, member1.getId());
System.out.println("refMember.getClass().getName() = " + refMember.getClass().getName()); // Proxy
```

- **프록시 클래스 확인 방법** 
  - `entity.getClass().getName() 출력(..javasist.. or HibernateProxy…)`

```java
Member refMember = em.getReference(Member.class, member1.getId());
Hibernate.initialize(refMember);
// refMember.getName(); // 이것도 강제 초기화하는 방법이 맞긴 함. Proxy 객체의 내부 메서드를 호출하는 것이기 때문에
```

- **프록시 강제 초기화** 
- Hibernate 에서 제공함
  - `org.hibernate.Hibernate.initialize(entity);`
- 참고: JPA 표준은 강제 초기화 없음 
  - 강제 호출: `member.getName()`

