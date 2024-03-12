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

## 즉시 로딩과 지연 로딩


### Member를 조회할 때 Team도 함께 조회해야 할까?

![프록시와 연관관계 관리1](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/e66b7852-1574-4671-989e-ed32503c45a9)

단순히 member 정보만 사용하는 비즈니스 로직

```
println(member.getName());
```

### 지연 로딩 LAZY을 사용해서 프록시로 조회

**`fetch = FetchType.LAZY`**

```java
@Entity
public class Member {

  @Id
  @GeneratedValue
  private Long id;

  @Column(name = "USERNAME")
  private String name;

  @ManyToOne(fetch = FetchType.LAZY) //**
  @JoinColumn(name = "TEAM_ID")
  private Team team;
  ...
}
```
```java
public static void main(String[] args) {
  Team team = new Team();
  team.setName("team1");
  em.persist(team);

  Member member = new Member();
  member.setName("member1");
  member.setTeam(team);
  em.persist(member);

  em.flush();
  em.clear();

  Member m = em.find(Member.class, member.getId());

  System.out.println("team.class = " + m.getTeam().getClass());

  System.out.println("==========");
  System.out.println("team.name = " + m.getTeam().getName());
  System.out.println("team.class = " + m.getTeam().getClass());
  System.out.println("==========");
}
```

```
Hibernate: 
    select
        m1_0.MEMBER_ID,
        m1_0.createdAt,
        m1_0.createdBy,
        m1_0.lastModifiedAt,
        m1_0.lastModifiedBy,
        m1_0.USERNAME,
        m1_0.TEAM_ID 
    from
        Member m1_0 
    where
        m1_0.MEMBER_ID=?
team.class = class hellojpa.Team$HibernateProxy$omkuRKXX
==========
Hibernate: 
    select
        t1_0.TEAM_ID,
        t1_0.createdAt,
        t1_0.createdBy,
        t1_0.lastModifiedAt,
        t1_0.lastModifiedBy,
        t1_0.name 
    from
        Team t1_0 
    where
        t1_0.TEAM_ID=?
team.name = team1
team.class = class hellojpa.Team$HibernateProxy$omkuRKXX
==========
```

- 처음에는 member 에 해당하는 값만 가져오게 된다. **team 을 proxy 로 가져온다.**
- m.getTEam().getName() 을 하자 (team을 실제로 사용하는 시점에)team 을 select 해온다.

![프록시와 연관관계 관리2](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/eac94996-aa83-4e3f-990f-f461060b04e5)

실제 팀을 사용하는 시점에서 초기화한다.
- -> `team.getName()` team 객체 내부가 빈 껍데기이기 때문에 getName() 과 같은 행위를 할 때 객체(여기서는 team) 을 초기화한다.

![프록시와 연관관계 관리3](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/75c83469-a044-40e1-ba5a-3b941aba364b)


### Member와 Team을 자주 함께 사용한다면?

![프록시와 연관관계 관리4](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/91febed2-0c26-498f-92a9-9d8473b528a2)

### 즉시 로딩 EAGER를 사용해서 함께 조회

**`fetch = FetchType.EAGER`**

```java
@Entity
public class Member { 
  @Id 
  @GeneratedValue 
  private Long id;
  
  @Column(name = "USERNAME") 
  private String name;
  
  @ManyToOne(fetch = FetchType.EAGER) //**
  @JoinColumn(name = "TEAM_ID") 
  private Team team;
  ...
}
```

```java
public static void main(String[] args) {
  Team team = new Team();
  team.setName("team1");
  em.persist(team);

  Member member = new Member();
  member.setName("member1");
  member.setTeam(team);
  em.persist(member);

  em.flush();
  em.clear();

  Member m = em.find(Member.class, member.getId());

  System.out.println("team.class = " + m.getTeam().getClass());

  System.out.println("==========");
  System.out.println("team.name = " + m.getTeam().getName());
  System.out.println("team.class = " + m.getTeam().getClass());
  System.out.println("==========");
}
```

```
Hibernate: 
    select
        m1_0.MEMBER_ID,
        m1_0.createdAt,
        m1_0.createdBy,
        m1_0.lastModifiedAt,
        m1_0.lastModifiedBy,
        m1_0.USERNAME,
        t1_0.TEAM_ID,
        t1_0.createdAt,
        t1_0.createdBy,
        t1_0.lastModifiedAt,
        t1_0.lastModifiedBy,
        t1_0.name 
    from
        Member m1_0 
    left join
        Team t1_0 
            on t1_0.TEAM_ID=m1_0.TEAM_ID 
    where
        m1_0.MEMBER_ID=?
team.class = class hellojpa.Team
==========
team.name = team1
team.class = class hellojpa.Team
==========
```

- EAGER 이기 때문에, member 와 team 을 한꺼번에 가져온다.
- 한꺼번에 가져오기 때문에, team 이 proxy 가 아닌 진짜 team 엔티티이다.

![프록시와 연관관계 관리5](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/a6b049ec-9323-4d58-b217-5facf816e11e)


### 즉시 로딩(EAGER), Member조회시 항상 Team도 조회

![프록시와 연관관계 관리6](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/0a6a628d-da2c-4d3a-b64c-1dacca5532ca)

JPA 구현체는 가능하면 조인(JOIN)을 사용해서 SQL 한번에 함께 조회


# 프록시와 즉시로딩 주의

- 가급적 지연 로딩만 사용(특히 실무에서)
- **즉시 로딩을 적용하면 예상하지 못한 SQL이 발생**

```java
public static void main(String[] args) {
  Team team1 = new Team();
  team1.setName("team1");
  em.persist(team1);

  Team team2 = new Team();
  team2.setName("team1");
  em.persist(team2);

  Member member1 = new Member();
  member1.setName("member1");
  member1.setTeam(team1);
  em.persist(member1);

  Member member2 = new Member();
  member2.setName("member2");
  member2.setTeam(team2);
  em.persist(member2);

  em.flush();
  em.clear();

//        Member m = em.find(Member.class, member1.getId());

  List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
}
```

```
 /* select
        m 
    from
        Member m */ select
            m1_0.MEMBER_ID,
            m1_0.createdAt,
            m1_0.createdBy,
            m1_0.lastModifiedAt,
            m1_0.lastModifiedBy,
            m1_0.USERNAME,
            m1_0.TEAM_ID 
        from
            Member m1_0
Hibernate: 
    select
        t1_0.TEAM_ID,
        t1_0.createdAt,
        t1_0.createdBy,
        t1_0.lastModifiedAt,
        t1_0.lastModifiedBy,
        t1_0.name 
    from
        Team t1_0 
    where
        t1_0.TEAM_ID=?
Hibernate: 
    select
        t1_0.TEAM_ID,
        t1_0.createdAt,
        t1_0.createdBy,
        t1_0.lastModifiedAt,
        t1_0.lastModifiedBy,
        t1_0.name 
    from
        Team t1_0 
    where
        t1_0.TEAM_ID=?
```

- **즉시 로딩**은 JPQL에서 ***N+1 문제***를 일으킨다.
  - em.find() 는 PK 로 조회하고, JPA 에서 내부적으로 최적화를 한다.
  - jpql 은 먼저 그대로 sql 로 번역을 한다.
    1. 먼저 member 만 db 에서 가져온다.
    2. team 이 EAGER 로 설정되어 있기 때문에, 각 member 에 대한 team 도 다 가져와야 한다.
    3. **select team sql 이 db 에 한번 더 날리게 된다!!!!!**

- **@ManyToOne, @OneToOne은 기본이 즉시 로딩**
  - **-> LAZY로 설정**
- @OneToMany, @ManyToMany는 기본이 지연 로딩

### N+1 문제 해결방법

일단 모든 연관관계를 LAZY 로 전부 설정한다.
1. `fetch join` 을 통해 필요할 때 값을 가져온다.
2. `@EntityGraph` 어노테이션을 통해 값을 가져온다.
3. `batch size` 를 통해 해결, 쿼리가 한번 더 날라가긴 한다.(대신 N+1 이 아닌 1+1 이 된다.)


## 지연 로딩 활용

![프록시와 연관관계 관리7](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/e7a79f2d-65b8-46a7-bab4-57de4aec2a17)

- **Member** 와 **Team** 은 자주 함께 사용 -> **즉시 로딩**
- **Member** 와 **Order** 는 가끔 사용 -> **지연 로딩**
- **Order** 와 **Product** 는 자주 함께 사용 -> **즉시 로딩**

### 위 내용들은 전부 이론적인거고 실무에서는 무조건 지연 로딩(FetchType.LAZY)으로 다 바르는게 좋다!!!!!

![프록시와 연관관계 관리8](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/da366682-6660-4c7e-864b-c10df0fee786)

![프록시와 연관관계 관리9](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/2597035d-20b3-4f34-9082-cc37ecb62749)

### 지연 로딩 활용 - 실무

### 모든 연관관계에 지연 로딩을 사용해라! 실무에서 즉시 로딩을 사용하지 마라!
- **`JPQL fetch join` 이나, 엔티티 그래프 기능(`@EntityGraph`)을** 사용해라! (뒤에서 설명)
- **즉시 로딩은 상상하지 못한 쿼리가 나간다.**


## 영속성 전이: CASCADE

![프록시와 연관관계 관리10](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/2965d528-b66b-4019-b41d-4adae4f06eb4)

- **특정 엔티티를 영속 상태(`persist`)로 만들 때 연관된 엔티티도 함께 영속 상태(`persist`)로 만들고 싶을 때 사용한다.**
  - **즉시 로딩(EAGER), 지연로딩(LAZY)과 같은 연관관계 설정과 전혀 관계가 없다!!!**
- 예: 부모 엔티티를 저장할 때 자식 엔티티도 함께 저장.
- **한다미로 부모를 저장(persist)할 때, 자식도 다 같이 저장(persist)하고 싶을 때 사용한다!!**

```java
public static void main(String[] args) {
  CascadeChild child1 = new CascadeChild();
  CascadeChild child2 = new CascadeChild();

  CascadeParent parent = new CascadeParent();
  parent.addChild(child1);
  parent.addChild(child2);

  em.persist(parent);
  em.persist(child1);
  em.persist(child2);
}
```

**원래의 코드에서는 em.persist() 를 객체 하나하나마다 다 호출해줬어야 했다.**
- 이때, em.persist(parent) 만 호출하면 child1, child2 는 저장되지 않는다.

```java
import jakarta.persistence.Entity;

@Entity
public class CascadeParent {
  @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST)
  private List<CascadeChild> childList = new ArrayList<>();
}

public static void main(String[] args) {
  CascadeChild child1 = new CascadeChild();
  CascadeChild child2 = new CascadeChild();

  CascadeParent parent = new CascadeParent();
  parent.addChild(child1);
  parent.addChild(child2);

  em.persist(parent);
}
```

```
Hibernate: 
    /* insert for
        hellojpa.CascadeParent */insert 
    into
        CascadeParent (name, id) 
    values
        (?, ?)
Hibernate: 
    /* insert for
        hellojpa.CascadeChild */insert 
    into
        CascadeChild (name, parent_id, id) 
    values
        (?, ?, ?)
Hibernate: 
    /* insert for
        hellojpa.CascadeChild */insert 
    into
        CascadeChild (name, parent_id, id) 
    values
        (?, ?, ?)
```

**`CascadeType.PERSIST` 설정을 하고나니, em.persist(parent); 만 하더라도 child1, child2 가 insert 되는 것을 볼 수 있다.**
- parent 를 persist 할 때, 밑에 있는 List<CascadeChild> 도 다 persist 를 해줄거야가 ***cascade*** 인 것이다!!!
- **한마디로, 연쇄작용이다!!!**

### 영속성 전이: 저장

![프록시와 연관관계 관리11](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/7b811735-7756-4e43-8bd6-b58b4abb76e6)

```
@OneToMany(mappedBy="parent", cascade= CascadeType.PERSIST)
```

### 영속성 전이: CASCADE - 주의!

- **영속성 전이(cascade)는 연관관계를 매핑하는 것과 아무 관련이 없음!!!**
- 엔티티를 영속화할 때 연관된 엔티티도 **함께 영속화**하는 편리함을 제공할 뿐

**child 와 같은 엔티티의 소유자가 parent 하나일 때만 cascade 를 사용하자!!!!!!**
- 예를 들어, member 가 child(member -> child) 와 연관관계가 있을 때는 쓰면 안된다.
  1. **lifeCycle 이 똑같을 때**, 예를 들어 parent 와 child 의 lifeCycle 이 유사할 때 
  2. **단일 엔티티에 종속적일 때(단일 소유자일 때)**, 예를 들어, member 나 다른 entity 에서 child 를 소유한다면 cascade 를 사용하면 안된다.

### CASCADE의 종류

lifeCycle 을 전부 맞춰야 할 때는 `ALL` 을 하고, 저장할 때만 같이 저장하고 싶을 때는 `PERSIST` 를 하자. 보통 이 2개의 옵션을 사용한다.

- **ALL: 모두 적용**
- **PERSIST: 영속**
- **REMOVE: 삭제**
- MERGE: 병합
- REFRESH: REFRESH
- DETACH: DETACH


## 고아 객체

- **고아 객체 제거: 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제**
- **`orphanRemoval = true`**

```java
import jakarta.persistence.Entity;

@Entity
public class CascadeParent {
  @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST, orphanRemoval = true)
  private List<CascadeChild> childList = new ArrayList<>();
}

public static void main(String[] args) {
  CascadeParent p = em.find(CascadeParent.class, parent.getId());
  p.getChildList().remove(0);
}
```

```
Hibernate: 
    select
        cp1_0.id,
        cp1_0.name 
    from
        CascadeParent cp1_0 
    where
        cp1_0.id=?
Hibernate: 
    select
        cl1_0.parent_id,
        cl1_0.id,
        cl1_0.name 
    from
        CascadeChild cl1_0 
    where
        cl1_0.parent_id=?
Hibernate: 
    /* delete for hellojpa.CascadeChild */delete 
    from
        CascadeChild 
    where
        id=?
```

**자식 엔티티를 컬렉션에서 제거**
- **컬렉션에서 제거한 객체는 자동적으로 delete 쿼리가 날라가서 삭제가 된다.**

### 고아 객체 - 주의

- 참조가 제거된 엔티티는 다른 곳에서 참조하지 않는 고아 객체로 보고 삭제하는 기능
- **참조하는 곳이 하나일 때 사용해야함!**
  - **특정 엔티티가 개인 소유할 때 사용**
  - cascade 와 마찬가지로 단일 entity 에 종속적일 때만 사용하자!!
- @OneToOne, @OneToMany만 가능

```java
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;

@Entity
public class CascadeParent {
  @OneToMany(mappedBy = "parent", cascade = CascadeType.PERSIST, orphanRemoval = true)
  private List<CascadeChild> childList = new ArrayList<>();
}

// 이렇게 설정해도 위와 같은 효과를 얻을 수 있다.
@Entity
public class CascadeParent {
  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  private List<CascadeChild> childList = new ArrayList<>();
}

public static void main(String[] args) {
  System.out.println("===== select CascadeParent =====");
  CascadeParent p = em.find(CascadeParent.class, parent.getId());
  System.out.println("===== select CascadeParent =====");

  System.out.println("===== delete CascadeChild =====");
  em.remove(p);
  System.out.println("===== delete CascadeChild =====");
}
```

```
===== select CascadeParent =====
Hibernate: 
    select
        cp1_0.id,
        cp1_0.name 
    from
        CascadeParent cp1_0 
    where
        cp1_0.id=?
===== select CascadeParent =====
===== delete CascadeChild =====
Hibernate: 
    select
        cl1_0.parent_id,
        cl1_0.id,
        cl1_0.name 
    from
        CascadeChild cl1_0 
    where
        cl1_0.parent_id=?
===== delete CascadeChild =====
Hibernate: 
    /* delete for hellojpa.CascadeChild */delete 
    from
        CascadeChild 
    where
        id=?
Hibernate: 
    /* delete for hellojpa.CascadeChild */delete 
    from
        CascadeChild 
    where
        id=?
Hibernate: 
    /* delete for hellojpa.CascadeParent */delete 
    from
        CascadeParent 
    where
        id=?
```

- 참고: 개념적으로 부모를 제거하면 자식은 고아가 된다. 따라서 고아 객체 제거 기능을 활성화 하면, 부모를 제거할 때 자식도 함께 제거된다. 
  - `em.remove(parent);` 를 하면, 
  - parent 에 대한 List<child> 를 select 해오고, 그것들을 delete 한다.
  - 그런 다음 마지막에 parent 도 삭제한다.
- 이것은 `CascadeType.REMOVE` 처럼 동작한다.


### 영속성 전이 + 고아 객체, 생명주기

- **CascadeType.ALL + orphanRemoval=true**
- 스스로 생명주기를 관리하는 엔티티는 em.persist()로 영속화, em.remove()로 제거
- 두 옵션을 모두 활성화 하면 **부모 엔티티**를 통해서 **자식의 생명주기**를 관리할 수 있음
- ***도메인 주도 설계(DDD)의 Aggregate Root 개념을 구현할 때 유용***
  - repository 는 aggregate root 만 컨택하고, 나머지는 repository 를 만들지 않는 것이 더 유용하다.
  - 나머지들은 aggregate root 를 통해 생명주기를 관리한다.

# 값 타입

> *임베디드 타입* 과 *값타입 컬렉션* 이 2가지가중요!!!

## 기본값 타입

### JPA의 데이터 타입 분류

**JPA 는 최상위 레벨로 보면 값 타입을 엔티티 타입과 값 타입의 2가지로 분류한다.**

- **엔티티 타입**
  - `@Entity`로 정의하는 객체
  - 데이터가 변해도 식별자(id)로 지속해서 **추적 가능**
  - 예) 회원 엔티티의 키나 나이 값을 변경해도 식별자(id 값)로 인식 가능
- **값 타입**
  - int, Integer, String처럼 단순히 값으로 사용하는 자바 기본 타입이나 객체
  - 식별자가 없고 값만 있으므로 변경시 추적 불가
  - 예) 숫자 100 을 200 으로 변경하면 완전히 다른 값으로 대체


### 값 타입 분류

- **기본값 타입**: JAVA 가 제공하는 기본적인 data type
  - 자바 기본 타입(int, double): primitive type: 원시 타입
  - 래퍼 클래스(Integer, Long)
  - String
- **임베디드 타입** (embedded type, 복합 값 타입): EX) (x,y) 와 같은 좌표를 다루는 Position class 를 만들 때를 말함
- **컬렉션 값 타입** (collection value type): EX) `private List<Child> children`

### 기본값 타입

- 예): String name, int age
- **생명주기를 엔티티에 의존한다!!!**
  - 예) 회원을 삭제하면 이름, 나이 필드도 함께 삭제
- **값 타입은 공유하면 X!!!**
  - 예) 회원 이름 변경시 다른 회원의 이름도 함께 변경되면 안됨

### 참고: 자바의 기본 타입은 절대 공유X

- int, double 같은 기본 타입(primitive type)은 절대 공유X
- 기본 타입은 항상 값을 복사함
- Integer같은 래퍼 클래스나 String 같은 특수한 클래스는 공유 가능한 객체이지만 변경X

## 임베디드 타입(복합 값 타입)

### 임베디드 타입

- 새로운 값 타입을 직접 정의할 수 있음
- JPA는 임베디드 타입(embedded type)이라 함
- 주로 기본 값 타입을 모아서 만들어서 복합 값 타입이라고도 함
- int, String과 같은 값 타입
  - **값 타입과 마찬가지로 변경하면, 추적이 불가능해진다.**

### 임베디드 타입에 대한 예시

> 회원 엔티티는 이름, 근무 시작일, 근무 종료일, 주소 도시, 주소 번지, 주소 우편번호를 가진다.
- 근무 시작일, 근무 종료일은 비슷하니까 하나로 묶고, 주소 도시, 주소 번지, 주소 우편번호도 비슷한 성격이니까 하나로 묶자.

![값 타입1](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/36fb8fb0-5744-44ef-80ad-e92e3ddda7a9)

> 회원 엔티티는 이름, 근무 기간, 집 주소를 가진다.
- 근무 기간(Period), 집 주소(Address) 로 묶어내었다.

![값 타입2](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/c91cac46-9ca2-4116-a0da-135711a42871)

> Period, Address 를 묶어내어서 값 타입으로 만들었다. 쉽게 말해서 class 로 만들었다.

![값타입3](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/8300b27b-262c-4447-a4a6-d2288dcabe62)

### 임베디드 타입 사용법

- `@Embeddable`: 값 타입을 정의하는 곳에 표시
- `@Embedded`: 값 타입을 사용하는 곳에 표시
- **기본 생성자 필수**


### 임베디드 타입의 장점

- **재사용성**
  - Period, Address 와 같은 class 들을 다른 클래스에서도 재사용할 수 있다.
- **높은 응집도**
- Period.isWork()처럼 해당 값 타입만 사용하는 의미 있는 메소드를 만들 수 있음
  - 상당히 객체지향적인 코드로 만들 수 있다.
- 임베디드 타입을 포함한 모든 값 타입은, 값 타입을 소유한 엔티티에 생명주기를 의존함
  - 임베디드 타입도 결국 값 타입이다.
  - 엔티티가 죽으면 다 죽는 것이고, 엔티티가 생성될 때 값이 들어오는 것이다.

### 임베디드 타입과 테이블 매핑

> DB 입장에서는 Period, Address 와 같은 임베디드 타입을 쓰든 값 타입을 쓰든 똑같다. 대신에 Mapping 만 조금 해주면 된다.

> DB 입장에서는 똑같지만, 객체 입장에서는 데이터 뿐만 아니라 메서드와 같은 기능, 행위까지 다 들고 있기 때문에, 묶었을 때 가질 수 있는 이득이 많아진다.

![값타입4](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/3a268bc0-a531-4995-8d19-1e7cd53db867)

### 임베디드 타입과 테이블 매핑

- 임베디드 타입은 엔티티의 값일 뿐이다.
- 임베디드 타입을 사용하기 전과 후에 **매핑하는 테이블은 같다!!!**
- **객체와 테이블을 아주 세밀하게(find-grained) 매핑하는 것이 가능**
- 잘 설계한 ORM 애플리케이션은 매핑한 테이블의 수보다 클래스의 수가 더 많음
  - 설계적으로 봤을 때도 모델링도 되게 깔끔하게 떨어지고, 설명하기도 좋다.
  - 이렇게 만들어 놓으면 공통으로 관리할 수 있고, domain 의 언어를 공통화할 수 있다. 언어도 공통이 되고 코드도 공통이 된다.

### 임베디드 타입과 연관관계

![값타입5](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/ca01325c-e53d-45a3-a06a-5edd6b8491e0)

### @AttributeOverride: 속성 재정의

```java
import jakarta.persistence.*;

@Entity
public class Member {
  @Embedded
  private Address homeAddress;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(name = "city", column = @Column(name = "WORK_CITY")),
    @AttributeOverride(name = "street", column = @Column(name = "WORK_STREET")),
    @AttributeOverride(name = "zipcode", column = @Column(name = "WORK_ZIPCODE"))
  })
  private Address workAddress;
}

```

```
Hibernate: 
    create table Member (
        MEMBER_ID bigint not null,
        ...
        endDate timestamp(6),
        startDate timestamp(6),
        ...
        WORK_CITY varchar(255),
        WORK_STREET varchar(255),
        WORK_ZIPCODE varchar(255),
        city varchar(255),
        street varchar(255),
        zipcode varchar(255),
    )
```

- 한 엔티티에서 같은 값 타입을 사용하면?
- 컬럼 명이 중복됨
- **`@AttributeOverrides`, `@AttributeOverride`** 를 사용해서 컬러 명 속성을 **재정의**

### 임베디드 타입과 null
- 임베디드 타입의 값이 null 이면 매핑한 컬럼 값은 모두 null


## 값 타입과 불변 객체

**값 타입은 복잡한 객체 세상을 조금이라도 단순화하려고 만든 개념이다. 따라서 값 타입은 단순하고 안전하게 다룰 수 있어야 한다.**

### 값 타입 공유 참조

![값타입6](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/85cec68b-c40b-431a-9c0f-c9b0a1ed6d60)

> 회원1 과 회원2 동일한 주소 값타입에 접근할 때, 부작용이 발생할 수 있다.

***임베디드 타입 같은 값 타입을 여러 엔티티에서 공유하면 위험함***
- 부작용(side effect) 발생

**EX)**

```java
public static void main(String[] args) {
  Address address = new Address("city", "street", "100000");

  Member member1 = new Member();
  member1.setName("member1");
  member1.setHomeAddress(address);
  em.persist(member1);

  Member member2 = new Member();
  member2.setName("member1");
  member2.setHomeAddress(address);
  em.persist(member2);

  // 당연히 예시 코드여서 이렇게 코드가 붙어있지만, 실제로는 member, address 생성 코드는 따로 있고, address 변경 코드도 비즈니스 로직에서 따로 떨어져 있다고 본다.
  
  member1.getHomeAddress().setCity("New City");
}
```

> update query 가 member1 에 대해 한번만 날라갈 것을 의도했으나, update query 가 2번 날라간다.

```
/* update
        for hellojpa.Member */update Member 
    set
        createdAt=?,
        createdBy=?,
        city=?,
        street=?,
        zipcode=?,
        endDate=?,
        startDate=?,
        lastModifiedAt=?,
        lastModifiedBy=?,
        USERNAME=?,
        TEAM_ID=? 
    where
        MEMBER_ID=?
Hibernate: 
    /* update
        for hellojpa.Member */update Member 
    set
        createdAt=?,
        createdBy=?,
        city=?,
        street=?,
        zipcode=?,
        endDate=?,
        startDate=?,
        lastModifiedAt=?,
        lastModifiedBy=?,
        USERNAME=?,
        TEAM_ID=? 
    where
        MEMBER_ID=?
```

> 만약 진짜로 의도하고 2개 다 변경하게 만들고 싶었다면, Address 를 임베디드 타입이 아닌, Entity 로 만들어야 맞다. 값 타입은 side effect 가 있으면 안된다.

# 값 타입 복사

![값타입7](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/8b34ee47-9848-476e-885e-90a086e0956e)

- 값 타입의 실제 인스턴스인 값을 공유하는 것은 위험
- **대신 값(인스턴스)를 복사해서 사용**

**EX)**

> 인스턴스를 복사해서 사용하는 예시

```java
public static void main(String[] args) {
  Address address = new Address("city", "street", "100000");
  
  ...
  Address copyAddress = new Address(address.getCity(), address.getStreet(), address.getZipcode());
  ...
  member2.setHomeAddress(copyAddress);
  ...
  member1.getHomeAddress().setCity("New City");
}
```

### 객체 타입의 한계

- 항상 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용을 피할 수 있다.
- 문제는 임베디드 타입처럼 **직접 정의한 값 타입은 자바의 기본 타입이 아니라 객체 타입 이다.**
- 자바 기본 타입에 값을 대입하면 값을 복사한다.
- **객체 타입은 참조 값을 직접 대입하는 것을 막을 방법이 없다.**
- **객체의 공유 참조는 피할 수 없다.**


### 객체 타입의 한계

기본 타입(primitive type)

> 기본 타입은 값을 복사

```java
int a = 10;
int b = a; // 기본 타입은 값을 복사
b = 4;
```

객체 타입

> 객체 타입은 참조를 전달

```java
Address a = new Address("Old");
Address b = a; //객체 타입은 참조를 전달
b.setCity("New");
```

### 불변 객체

- 객체 타입을 수정할 수 없게 만들면 **부작용을 원천 차단**
- **값 타입은 불변 객체(immutable object)로 설계해야함**
- **불변 객체: 생성 시점 이후 절대 값을 변경할 수 없는 객체**

***생성자로만 값을 설정하고 수정자(Setter)를 만들지 않으면 됨***
>참고: Integer, String은 자바가 제공하는 대표적인 불변 객체 

> Setter 를 전부 없애거나, private 으로 만들어버리자. 필요하다면 완전히 새로 만들어서 바꿔버리자.

**값 타입은 그냥 깔끔하게 불변으로 만들어버리자!!! 불변이라는 작은 제약으로 부작용이라는 큰 재앙을 막을 수 있다.**


## 값 타입의 비교

- 값 타입: 인스턴스가 달라도 그 안에 값이 같으면 같은 것으로 봐야 함

```java
int a = 10;
int b = 10;
```
```java
Address a = new Address("서울시");
Address b = new Address("서울시");
```

- **동일성(identity) 비교** : 인스턴스의 참조 값을 비교, `==` 사용
- **동등성(equivalence) 비교** : 인스턴스의 값을 비교, `equals()` 사용
- 값 타입은 `a.equals(b)`를 사용해서 **동등성 비교**를 해야 함
***값 타입의 `equals()` 메소드를 적절하게 재정의하자!!!(주로 모든 필드 사용)***

**EX) equals() and hashCode()**

```java
import hellojpa.Address;
import jakarta.persistence.Embeddable;

@Embeddable
public class Address {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Address address = (Address) o;
    return Objects.equals(city, address.city) && Objects.equals(street, address.street) && Objects.equals(zipcode, address.zipcode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(city, street, zipcode);
  }
}

public static void main(String[] args) {
  Address address1 = new Address("city", "street", "100000");
  Address address2 = new Address("city", "street", "100000");

  Address.equals(address2);
}
```

> 위의 예시에서는 city, street, zipcode 등 field 값들에 직접 접근했지만, 
> 나중에 Proxy, 다형성 등을 사용하게 되면, getter 를 통해 field 를 가져오는 것으로 변경해야 한다.

![Use getters during code generation](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/ea4cba08-1034-41d6-9af3-6de1dd893f2f)

## 값 타입 컬렉션

![값타입12](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/8558b801-588d-48fb-ab17-66709ebdbb60)

> 단순하게 값 타입이 하나일 때는 그냥 class 의 field 속성으로 해서 table 안에 값을 넣으면 된다. 

> 문제는 Collection 을 DB table 에 넣을 수 없다. RDB 는 기본적으로 table 안에 Collection 을 담을 수 있는 구조가 없다. Value 로 값만 넣을 수 있다.

> 컬렉션을 저장하기 위한 **별도의 테이블**이 필요하다. 구조적으로 보면 1:N 의 관계가 된다.

필드의 값들과 외래키를 전부 조합하여 PK 로 사용해야 한다. 별도의 id(`@Id`) 를 생성해서 PK 로 다루면, entity 가 되어버린다.

- 값 타입을 하나 이상 저장할 때 사용
- **`@ElementCollection`**, **`@CollectionTable`** 사용
- 데이터베이스는 컬렉션을 같은 테이블에 저장할 수 없다.

### 값 타입 컬렉션 사용


#### 값 타입 저장 예제

```java
import jakarta.persistence.*;

@Entity
public class Member {
  @Embedded
  private Address homeAddress;

  @ElementCollection
  @CollectionTable(
    name = "FAVORITE_FOOD",
    joinColumns = @JoinColumn(name = "MEMBER_ID")
  )
  @Column(name = "FOOD_NAME")
  private Set<String> favoriteFoods = new HashSet<>();

  @ElementCollection
  @CollectionTable(
    name = "ADDRESS",
    joinColumns = @JoinColumn(name = "MEMBER_ID")
  )
  private List<Address> addressHistory = new ArrayList<>();
}

public static void main(String[] args) {
  Member member = new Member();
  member.setName("member1");
  member.setHomeAddress(new Address("new", "street", "100000"));

  member.getFavoriteFoods().add("국밥");
  member.getFavoriteFoods().add("돈까스");
  member.getFavoriteFoods().add("햄버거");

  member.getAddressHistory().add(new Address("old1", "street", "100000"));
  member.getAddressHistory().add(new Address("old2", "street", "100000"));

  em.persist(member);
}
```

```
Hibernate: 
    create table Member (
        MEMBER_ID bigint not null,
        TEAM_ID bigint,
        USERNAME varchar(255),
        city varchar(255),
        street varchar(255),
        zipcode varchar(255),
        primary key (MEMBER_ID)
    )
    
Hibernate: 
    create table FAVORITE_FOOD (
        MEMBER_ID bigint not null,
        FOOD_NAME varchar(255)
    )
    
Hibernate: 
    create table ADDRESS (
        MEMBER_ID bigint not null,
        city varchar(255),
        street varchar(255),
        zipcode varchar(255)
    )
    
    
    Hibernate: 
    /* insert for
        hellojpa.Member */insert 
    into
        Member (city, street, zipcode, USERNAME, TEAM_ID, MEMBER_ID) 
    values
        (?, ?, ?, ?, ?, ?)
Hibernate: 
    /* insert for
        hellojpa.Member.addressHistory */insert 
    into
        ADDRESS (MEMBER_ID, city, street, zipcode) 
    values
        (?, ?, ?, ?)
Hibernate: 
    /* insert for
        hellojpa.Member.addressHistory */insert 
    into
        ADDRESS (MEMBER_ID, city, street, zipcode) 
    values
        (?, ?, ?, ?)
Hibernate: 
    /* insert for
        hellojpa.Member.favoriteFoods */insert 
    into
        FAVORITE_FOOD (MEMBER_ID, FOOD_NAME) 
    values
        (?, ?)
Hibernate: 
    /* insert for
        hellojpa.Member.favoriteFoods */insert 
    into
        FAVORITE_FOOD (MEMBER_ID, FOOD_NAME) 
    values
        (?, ?)
Hibernate: 
    /* insert for
        hellojpa.Member.favoriteFoods */insert 
    into
        FAVORITE_FOOD (MEMBER_ID, FOOD_NAME) 
    values
        (?, ?)
```

> `em.persist(member);` 만 해도 AddressHistory, FavoriteFood 가 다 저장이 된다. 
> lifeCycle 이 member 에 다 의존하고 있다. 값 타입 및 값 타입 컬렉션은 주인 엔티티(여기선 member) 에 lifeCycle 을 다 의존하고 있다.
> `(CascadeType.ALL, orphanRemoval = true)` 를 킨 것과 똑같은 효과를 본다.

#### 값 타입 조회 예제

```java
public static void main(String[] args) {
  em.flush();
  em.clear();

  System.out.println("========== findMember ==========");

  Member findMember = em.find(Member.class, member.getId());
  System.out.println("========== findMember ==========");

  System.out.println("========== getAddressHistory ==========");
  List<Address> addresses = findMember.getAddressHistory();
  for (Address address : addresses) {
    System.out.println("address = " + address.getCity());
  }
  System.out.println("========== getAddressHistory ==========");

  System.out.println("========== getFavoriteFoods ==========");
  Set<String> favoriteFoods = findMember.getFavoriteFoods();
  for (String favoriteFood : favoriteFoods) {
    System.out.println("favoriteFood = " + favoriteFood);
  }
  System.out.println("========== getFavoriteFoods ==========");
}
```

```
========== findMember ==========
Hibernate: 
    select
        m1_0.MEMBER_ID,
        m1_0.city,
        m1_0.street,
        m1_0.zipcode,
        m1_0.USERNAME,
        m1_0.TEAM_ID 
    from
        Member m1_0 
    where
        m1_0.MEMBER_ID=?
========== findMember ==========
========== getAddressHistory ==========
Hibernate: 
    select
        ah1_0.MEMBER_ID,
        ah1_0.city,
        ah1_0.street,
        ah1_0.zipcode 
    from
        ADDRESS ah1_0 
    where
        ah1_0.MEMBER_ID=?
address = old1
address = old2
========== getAddressHistory ==========
========== getFavoriteFoods ==========
Hibernate: 
    select
        ff1_0.MEMBER_ID,
        ff1_0.FOOD_NAME 
    from
        FAVORITE_FOOD ff1_0 
    where
        ff1_0.MEMBER_ID=?
favoriteFood = 돈까스
favoriteFood = 햄버거
favoriteFood = 국밥
========== getFavoriteFoods ==========
```

> 첫 쿼리를 보면 findMember 를 하면 member 만 가져온다. `@ElementCollection` 은 기본 전략이 `fetch = FetchType.LAZY` 이다. 지연 로딩 전략이다.
- 값 타입 컬렉션도 **지연 로딩 전략(`fetch = FetchType.LAZY`)** 사용
- 참고: 값 타입 컬렉션은 영속성 전에(Cascade) + 고아 객체 제거 기능을 필수로 가진다고 볼 수 있다.

#### 값 타입 수정 예제

```java
public static void main(String[] args) {
  em.flush();
  em.clear();

  Member findMember = em.find(Member.class, member.getId());

  // 치킨 -> subway
  findMember.getFavoriteFoods().remove("국밥");
  findMember.getFavoriteFoods().add("subway");

  // old1 -> oldCity
  findMember.getAddressHistory().remove(new Address("old1", "street", "100000"));
  findMember.getAddressHistory().add(new Address("oldCity", "street", "100000"));
}
```

- remove(Object o) 를 할 때, instance 를 사용해서 제거할 수 있다. 
  - JPA 에서 내부적으로 override 메서드인 equals 를 사용하여, 객체를 비교 후에 똑같은 값을 가진 객체를 삭제해준다.
  - **그렇기 떄문에, equals() and hashCode() 메서드를 잘 override 해야한다!**
  - **주로 컬렉션(Collection) 을 다룰 때, equals() and hashCode() 메서드가 효과를 본다.**

```
Hibernate: 
    /* one-shot delete for hellojpa.Member.addressHistory */delete 
    from
        ADDRESS 
    where
        MEMBER_ID=?
Hibernate: 
    /* insert for
        hellojpa.Member.addressHistory */insert 
    into
        ADDRESS (MEMBER_ID, city, street, zipcode) 
    values
        (?, ?, ?, ?)
Hibernate: 
    /* insert for
        hellojpa.Member.addressHistory */insert 
    into
        ADDRESS (MEMBER_ID, city, street, zipcode) 
    values
        (?, ?, ?, ?)
        
-----------------------------------------------------------        
        
Hibernate: 
    /* delete for hellojpa.Member.favoriteFoods */delete 
    from
        FAVORITE_FOOD 
    where
        MEMBER_ID=? 
        and FOOD_NAME=?
Hibernate: 
    /* insert for
        hellojpa.Member.favoriteFoods */insert 
    into
        FAVORITE_FOOD (MEMBER_ID, FOOD_NAME) 
    values
        (?, ?)
```

> Hibernate:
> /* one-shot delete for hellojpa.Member.addressHistory */delete
> from
> ADDRESS
> where
> MEMBER_ID=?
 
> 식별자가 없기 떄문에, 값 타입 컬렉션은 추적이 어렵다.
 
> **값 타입 컬렉션에 변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제하고, 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장한다.**

- ***결론은 값 타입 컬렉션은 엄청 간단한 값을 저장하는 경우가 아니라면 쓰지 말자!!!***
- 진짜 쓴다면, 모든 field 들을 묶어서 따로 PK 로 만들어줘서 사용해야 한다.

```java
import jakarta.persistence.Entity;

@Entity
public class Member {
  @OrderColumn(name = "address_history_order")
  @ElementCollection
  @CollectionTable(
    name = "ADDRESS",
    joinColumns = @JoinColumn(name = "MEMBER_ID")
  )
  private List<Address> addressHistory = new ArrayList<>();
}
```

- **`@OrderColumn`** 을 사용할 수 있지만, 이것도 순서가 꼬여버리면, 예를 들어, 0,1,2,3 중에 2 를 빼먹으면, null 값이 들어가버릴 수도 있다.

### 값 타입 컬렉션의 제약사항

- 값 타입은 엔티티와 다르게 식별자 개념이 없다.
- 값은 변경하면 추적이 어렵다.
- **값 타입 컬렉션에 변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제하고, 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장한다.**
- 값 타입 컬렉션을 매핑하는 테이블은 모든 컬럼을 묶어서 기본키를 구성해야 함: **null 입력X, 중복 저장X**

### 값 타입 컬렉션 대안

```java
import jakarta.persistence.*;

@Entity
public class Member {
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "MEMBER_ID")
  private List<AddressEntity> addressHistory = new ArrayList<>();
}

@Entity
public class AddressEntity {
  @Id @GeneratedValue
  private Long id;
  private Address address;
  
  public AddressEntity() {
  }

  public AddressEntity(Address address) {
    this.address = address;
  }

  public AddressEntity(String city, String street, String zipcode) {
    this.address = new Address(city, street, zipcode);
  }
  ...
}

public static void main(String[] args) {
  // old1 -> oldCity
  findMember.getAddressHistory().remove(new AddressEntity("old1", "street", "100000"));
  findMember.getAddressHistory().add(new AddressEntity("oldCity", "street", "100000"));
}
```

> Address(값 타입) 을 field 로 가지는 entity(AddressEntity) 를 생성하고, Member 와 `@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)` 관계로 풀어내었다.

- 실무에서는 상황에 따라 값 타입 컬렉션 대신에 **일대다 관계를 고려**
- 일대다 관계를 위한 **엔티티**를 만들고, **여기에서 값 타입을 사용**
- 영속성 전이(Cascade) + 고아 객체 제거를 사용해서 값 타입 컬렉션 처럼 사용
- EX) AddressEntity

### 정리

- **엔티티 타입의 특징**
  - 식별자O
  - 생명 주기 관리
  - 공유
- **값 타입의 특징**
  - **식별자X**
  - 생명 주기를 엔티티에 의존
  - 공유하지 않는 것이 안전(복사해서 사용)
  - 불변 객체로 만드는 것이 안전

***결론적으로 값 타입은, select box 결과와 같은 진짜 간단한 곳에 사용할 수 있다. 그 외에는 entity 로 만들어서 사용하자!!***

***식별자가 필요하고, 지속해서 값을 추적, 변경해야 한다면 그것은 값 타입이 아닌 엔티티***

- 값 타입은 정말 값 타입이라 판단될 때만 사용
- 엔티티와 값 타입을 혼동해서 엔티티를 값 타입으로 만들면 안됨

# 객체지향 쿼리 언어(JPQL)

# 객체지향 쿼리 언어 소개

## JPA는 다양한 쿼리 방법을 지원

- **JPQL**
- JPA Criteria
- **QueryDSL**
- 네이티브 SQL
- JDBC API 직접 사용, MyBatis, SpringJdbcTemplate 함께 사용


### JPQL 소개

- 가장 단순한 조회 방법
  - EntityManager.find()
  - 객체 그래프 탐색(a.getB().getC())
- **나이가 18 살 이상인 회원을 모두 검색하고 싶다면?**


## JPQL: 엔티티 객체를 대상으로 하는 객체 지향 SQL

- SQL 과 굉장히 유사한 문법이 제공
- JPA를 사용하면 **엔티티 객체를 중심으로 개발**
- 문제는 검색 쿼리
- 검색을 할 때도 **테이블이 아닌 엔티티 객체를 대상으로 검색**
- 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL이 필요
- JPA는 **SQL을 추상화**한 JPQL이라는 ***객체 지향 쿼리 언어 제공***
- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원

### JPQL 과 SQL

- JPQL은 **엔티티 객체**를 대상으로 쿼리
- SQL은 **데이터베이스 테이블**을 대상으로 쿼리
- SQL을 추상화해서 특정 데이터베이스 SQL에 의존 X
- JPQL을 한마디로 정의하면 **객체 지향 SQL**


```java
public static void main(String[] args) {
  List<Member> result = em.createQuery("select m from Member m where m.name like '%lee%'", Member.class).getResultList();
}
```

- `select m` 이라고 하는 것은 **member entity 자체**를 조회 해온다는 뜻이다. 

```
Hibernate: 
    /* select
        m 
    from
        Member m 
    where
        m.name like '%lee%' */ select
            m1_0.MEMBER_ID,
            m1_0.city,
            m1_0.street,
            m1_0.zipcode,
            m1_0.USERNAME,
            m1_0.TEAM_ID 
        from
            Member m1_0 
        where
            m1_0.USERNAME like '%lee%' escape ''
```

그러나 동적쿼리를 짜는데, jpql 은 많이 불편하다.

## Criteria 소개

```java
//Criteria 사용 준비
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<Member> query = cb.createQuery(Member.class);

//루트 클래스 (조회를 시작할 클래스)
Root<Member> m = query.from(Member.class);

//쿼리 생성 
CriteriaQuery<Member> cq = query.select(m).where(cb.equal(m.get("username"), "kim"));
List<Member> resultList = em.createQuery(cq).getResultList();
```

```
Hibernate: 
    /* <criteria> */ select
        m1_0.MEMBER_ID,
        m1_0.city,
        m1_0.street,
        m1_0.zipcode,
        m1_0.USERNAME,
        m1_0.TEAM_ID 
    from
        Member m1_0 
    where
        m1_0.USERNAME=?
```

- 문자가 아닌 자바코드로 JPQL을 작성할 수 있음
- JPQL 빌더 역할
- JPA 공식 기능
- **단점: 너무 복잡하고 실용성이 없다.**
  - sql 스럽지가 않다.
- Criteria 대신에 **QueryDSL 사용 권장**


## QueryDSL 소개

```java
//JPQL
//select m from Member m where m.age > 18
JPAFactoryQuery query = new JPAQueryFactory(em);
QMember m = QMember.member;

List<Member> list = query.selectFrom(m)
  .where(m.age.gt( 18 ))
  .orderBy(m.name.desc())
  .fetch();
```

- 문자가 아닌 **자바코드**로 **JPQL**을 작성할 수 있음
- JPQL 빌더 역할
- 컴파일 시점에 문법 오류를 찾을 수 있음
- ***동적쿼리!!!!!*** 작성 편리함
- 단순하고 쉬움
- ***실무 사용 권장!!!!!***


## 네이티브 SQL 소개

- JPA가 제공하는 SQL을 직접 사용하는 기능
- JPQL로 해결할 수 없는 특정 데이터베이스에 의존적인 기능
- 예) 오라클 CONNECT BY, 특정 DB만 사용하는 SQL 힌트

```java
List<Member> resultList = em.createNativeQuery("SELECT MEMBER_ID, city, street, zipcode, USERNAME, TEAM_ID FROM MEMBER WHERE USERNAME = 'lee'", Member.class).getResultList();
```
```
Hibernate: 
    /* dynamic native SQL query */ SELECT
        MEMBER_ID,
        city,
        street,
        zipcode,
        USERNAME,
        TEAM_ID 
    FROM
        MEMBER 
    WHERE
        USERNAME = 'lee'
```

## JDBC 직접 사용, SpringJdbcTemplate 등

***단, 영속성 컨텍스트를 적절한 시점에 강제로 플러시(`em.flush()`) 필요***

- JPA를 사용하면서 JDBC 커넥션을 직접 사용하거나, 스프링 JdbcTemplate, 마이바티스등을 함께 사용 가능
- 예) JPA를 우회해서 SQL을 실행하기 직전에 영속성 컨텍스트 수동 플러시

## JPQL(Java Persistence Query Language)


### JPQL - 기본 문법과 기능

### JPQL 소개 - JPQL은 SQL을 추상화해서 특정 데이터베이스 SQL에 의존하지 않는다.

- **JPQL은 객체지향 쿼리 언어**다. 따라서 테이블을 대상으로 쿼리 하는 것이 아니라 **엔티티 객체를 대상으로 쿼리 한다.**
- **JPQL은 SQL을 추상화해서 특정 데이터베이스 SQL에 의존하지 않는다.**
- JPQL은 결국 SQL로 변환된다.

![객체지향 쿼리 언어1](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/f6d808e7-a325-4bca-8654-35ca400d4d0c)

# JPQL 문법

![객체지향 쿼리 언어2](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/1d50005e-70cf-4968-97e3-ff9b1230bd1d)
- update, delete 문은 **bulk 연산**에 사용된다.
  - 한방에 여러개의 data 를 변경할 때 사용한다.
    - jpa 는 원래 값이 변경되면 트랜잭션 커밋 시점에 update 쿼리를 날린다.
    - 다만, 이것은 한건 한건씩 변경되는 것이고, 여러개를 한번에 변경할 경우에는 bulk 연산을 사용한다.
    - bulk 연산은 jpa 에서 따로 관리한다.

# JPQL 문법

- select m from **Member** as m where **m.age** > 18
- 엔티티와 속성은 대소문자 구분 O (Member, age)
- JPQL 키워드는 대소문자 구분 X (SELECT, FROM, where)
- 엔티티 이름 사용, 테이블 이름이 아님(Member)
- **별칭은 필수(m)** (as는 생략가능)


# 집합과 정렬

```sql
select
    COUNT (m), //회원수
    SUM (m.age), //나이 합
    AVG (m.age), //평균 나이
    MAX (m.age), //최대 나이
    MIN (m.age) //최소 나이
from Member m
```

### 집합과 정렬

- GROUP BY, HAVING
- ORDER BY


### TypedQuery, Query

- TypedQuery: 반환 타입이 명확할 때 사용
- Query: 반환 타입이 명확하지 않을 때 사용

```java
TypedQuery<Member> query1 = em.createQuery("select m from Member m", Member.class);
TypedQuery<String> query2 = em.createQuery("select m.username from Member m", String.class);
Query query3 = em.createQuery("select m.username, m.age from Member m");
```
- projection 을 사용하는 방법도 있긴 하다.

### 결과 조회 API

- `query.getResultList()`: **결과가 하나 이상일 때** , 리스트 반환
  - 결과가 없으면 빈 리스트 반환
- `query.getSingleResult()`: **결과가 정확히 하나** , 단일 객체 반환
  - 결과가 없으면: `javax.persistence.NoResultException`
  - 둘 이상이면: `javax.persistence.NonUniqueResultException`
  - getSingleResutl() 는 진짜로 결과가 딱 1개만 있을 때 사용해야 한다.
  - 사실, spring data jpa 의 내부 코드를 보면 getSingleResult() 를 한 다음에 try catch() 문을 사용한다.


# 파라미터 바인딩 - 이름 기준, 위치 기준

**이름 기준 파라미터 바인딩**
```java
TypedQuery<Member> query = em.createQuery("select m from Member m where m.username = :username", Member.class);
query.setParameter("username", "member1");
Member singleResult = query.getSingleResult();
System.out.println("singleResult.getAge() = " + singleResult.getAge());
```

```
Hibernate: 
    /* select
        m 
    from
        Member m 
    where
        m.username = :username */ select
            m1_0.id,
            m1_0.age,
            m1_0.TEAM_ID,
            m1_0.username 
        from
            Member m1_0 
        where
            m1_0.username=?
singleResult.getAge() = 28
```

**위치 기준 파라미터 바인딩**
```java
SELECT m FROM Member m where m.username= ?1

query.setParameter(1 ,usernameParam);
```

- 위치 기준 파라미터 바인딩은 웬만하면 쓰지말자. 
- 순서가 바뀐다면, 다 틀어지기 때문에 안쓰는 것을 추천한다.

## 프로젝션

- **SELECT 절에 조회할 대상을 지정하는 것**
- **프로젝션 대상: 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자등 기본 데이터 타입)**


#### SELECT m FROM Member m -> 엔티티 프로젝션(entity projection)

```java
public static void main(String[] args) {
  Member member = new Member();
  member.setUsername("member1");
  member.setAge(28);
  em.persist(member);

  em.flush();
  em.clear();

  List<Member> result = em.createQuery("select m from Member m", Member.class)
    .getResultList();

  Member findMember = result.get(0);
  findMember.setAge(11);
}
```

- result 와 같은 **엔티티 프로젝션**의 값으로(select 문의 쿼리 결과)로 나오는 값들은 전부 영속성 컨텍스트에 관리가 되어서 값을 바꾸면 변경 감지를 통해서 update 쿼리가 날라간다.  

#### SELECT **m.team** FROM Member m -> 엔티티 프로젝션(entity projection)

```java
public static void main(String[] args) {
  Member member = new Member();
  member.setUsername("member1");
  member.setAge(28);
  em.persist(member);

  em.flush();
  em.clear();

  List<Team> result = em.createQuery("select m.team from Member m", Team.class)
    .getResultList();
}
```

```
Hibernate: 
    /* select
        m.team 
    from
        Member m */ select
            t1_0.id,
            t1_0.name 
        from
            Member m1_0 
        join
            Team t1_0 
                on t1_0.id=m1_0.TEAM_ID 
```

```java
List<Team> result = em.createQuery("select t from Member m join m.team t", Team.class)
        .getResultList();
```

- team 은 다른 엔티티이기 때문에, 자동으로 join 쿼리가 날라간다.
- 경로표현식과 관련이 있다.
- jpql 은 웬만하면 sql 과 비슷하게 쓰는 것이 낫다.
  - join 이라는 것 자체가 성능에 영향을 줄 수 있는 요소가 많기 때문에 한눈에 보이게 하는 것이 낫다.
  - `m.team` 이런 식으로만 jpql 을 짜놓으면 실제로 sql query 가 어떻게 날라갈지 예상이 안가게 된다.
  - 뒤에 경로 표현식에서 명시적 조인, 묵시적 조인이 나온다.

***결론은 join 은 웬만하면 명시적 join 을 사용하자!!!!!***

#### SELECT **m.address** FROM Member m -> 임베디드 타입 프로젝션(embedded type projection) 

```java
em.createQuery("select o.address from Order o", Address.class)
          .getResultList();
```

```
Hibernate: 
    /* select
        o.address 
    from
        
    Order o */ select
        o1_0.city,
        o1_0.street,
        o1_0.zipcode from
            ORDERS o1_0
```

- 임베디드 타입은 entity 에 소속이 되어있기 때문에 `select address from Address` 와 같이는 사용하지 못한다.
- entity 를 명시해줘야 하는 한계가 있다.

#### SELECT **m.username, m.age** FROM Member m -> 스칼라 타입 프로젝션(scalar type projection)

```java
em.createQuery("select distinct m.username, m.age from Member m")
          .getResultList();
```

```
Hibernate: 
    /* select
        distinct m.username,
        m.age 
    from
        Member m */ select
            distinct m1_0.username,
            m1_0.age 
        from
            Member m1_0
```

- DISTINCT 로 중복 제거

### 프로젝션 - 여러 값 조회

- SELECT **m.username** , **m.age** FROM Member m

#### 1. Query 타입으로 조회

```java
Query query = em.createQuery("select m.username, m.age from Member m");
```

#### 2. Object[] 타입으로 조회

```java
import java.util.List;

public static void main(String[] args) {
  List resultList = em.createQuery("select m.username, m.age from Member m")
    .getResultList();
  Object o = resultList.get(0);
  Object[] result = (Object[]) o;
  System.out.println("username = " + result[0]);
  System.out.println("username = " + result[1]);

  List<Object[]> resultList2 = em.createQuery("select m.username, m.age from Member m")
    .getResultList();

  Object[] result2 = resultList2.get(0);
  System.out.println("username = " + result2[0]);
  System.out.println("age = " + result2[1]);
}
```

```
Hibernate: 
    /* select
        m.username,
        m.age 
    from
        Member m */ select
            m1_0.username,
            m1_0.age 
        from
            Member m1_0
username = member1
username = 28
Hibernate: 
    /* select
        m.username,
        m.age 
    from
        Member m */ select
            m1_0.username,
            m1_0.age 
        from
            Member m1_0
username = member1
age = 28
```

Object 로 TypeCasting 을 해서 사용한다.
- Type 에 대한 명시를 못하니까 Object 로 돌려주는 것

#### 3. new 명령어로 조회

```java
public static void main(String[] args) {
  List<MemberDTO> result = em.createQuery("select new org.example.jpql.domain.MemberDTO(m.username, m.age) from Member m", MemberDTO.class)
    .getResultList();

  MemberDTO memberDTO = result.get(0);
  System.out.println("username = " + memberDTO.getUsername());
  System.out.println("age = " + memberDTO.getAge());
}
```

```
Hibernate: 
    /* select
        new org.example.jpql.domain.MemberDTO(m.username, m.age) 
    from
        Member m */ select
            m1_0.username,
            m1_0.age 
        from
            Member m1_0
username = member1
age = 28
```

- 단순 값을 DTO로 바로 조회
  - SELECT **new** jpabook.jpql.UserDTO(m.username, m.age) FROM Member m
- **패키지 명을 포함한 전체 클래스명 입력**
- 순서와 타입이 일치하는 **생성자 필요**
- 단점은 패키지명이 길어지면 모두 다 적어야 한다.
  - select 문 문자이기 때문에 한계가 있다.(패키지명, new 생성자를 모두 써야 함.)
  - QueryDSL 로 극복이 가능하다.


## 페이징 API

- JPA는 페이징을 다음 두 API로 추상화
- **setFirstResult** (int startPosition) : 조회 시작 위치 (0부터 시작)
- **setMaxResults** (int maxResult) : 조회할 데이터 수


### 페이징 API 예시

```java
public static void main(String[] args) {
  for (int i = 1; i <= 100; i++) {
    Member member = new Member();
    member.setUsername("member" + i);
    member.setAge(i);
    em.persist(member);
  }

  em.flush();
  em.clear();

  // 페이징 쿼리
  List<Member> result = em.createQuery("select m from Member m order by m.age desc", Member.class)
    .setFirstResult(1)
    .setMaxResults(10)
    .getResultList();

  System.out.println("result.size = " + result.size());
  for (Member member : result) {
    System.out.println("member = " + member);
  }
}
```

```
Hibernate: 
    /* select
        m 
    from
        Member m 
    order by
        m.age desc */ select
            m1_0.id,
            m1_0.age,
            m1_0.TEAM_ID,
            m1_0.username 
        from
            Member m1_0 
        order by
            m1_0.age desc 
        offset
            ? rows 
        fetch
            first ? rows only
```

### 페이징 API - MySQL 방언

```xml
<property name="jakarta.persistence.jdbc.url" value="jdbc:h2:tcp://localhost/~/test;MODE=MySql"/>
<property name="hibernate.dialect" value="org.hibernate.dialect.MySQLDialect"/>
```

```java
public static void main(String[] args) {
  List<Member> result = em.createQuery("select m from Member m order by m.age desc", Member.class)
    .setFirstResult(1)
    .setMaxResults(10)
    .getResultList();
}
```

```
Hibernate: 
    /* select
        m 
    from
        Member m 
    order by
        m.age desc */ select
            m1_0.MEMBER_ID,
            m1_0.age,
            m1_0.TEAM_ID,
            m1_0.username 
        from
            Member m1_0 
        order by
            m1_0.age desc 
        limit
            ?, ?
```

### 페이징 API - Oracle 방언

![객체지향 쿼리 언어4](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/ca87a07b-2a1b-41d1-be2f-8f8f4df1c545)


# 조인

- 내부 조인: SELECT m FROM Member m [INNER] JOIN m.team t
- 외부 조인: SELECT m FROM Member m LEFT [OUTER] JOIN m.team t
- 세타 조인: select count(m) from Member m, Team t where m.username = t.name


# 조인 - ON 절

- ON절을 활용한 조인(JPA 2.1부터 지원)
- 1. 조인 대상 필터링
- 2. 연관관계 없는 엔티티 외부 조인(하이버네이트 5.1부터)


# 1. 조인 대상 필터링

- 예) 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조인
- JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'A'
- SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='A'

# 2. 연관관계 없는 엔티티 외부 조인

- 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
- JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
- SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name

# 서브 쿼리

- 나이가 평균보다 많은 회원

```
select m from Member m where m.age > (select avg(m2.age) from Member m2)
```
- 한 건이라도 주문한 고객

```
select m from Member m where **(select count(o) from Order o where m = o.member)** > 0
```

# 서브 쿼리 지원 함수

- [NOT] EXISTS (subquery): 서브쿼리에 결과가 존재하면 참
  - {ALL | ANY | SOME} (subquery)
  - ALL 모두 만족하면 참
  - ANY, SOME: 같은 의미, 조건을 하나라도 만족하면 참
- [NOT] IN (subquery): 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참


# 서브 쿼리 - 예제

- 팀A 소속인 회원
  - select m from Member m where **exists** (select t from m.team t where t.name = ‘팀A')
- 전체 상품 각각의 재고보다 주문량이 많은 주문들
  - select o from Order o where o.orderAmount > **ALL** (select p.stockAmount from Product p)
- 어떤 팀이든 팀에 소속된 회원
  - select m from Member m where m.team = **ANY** (select t from Team t)


# JPA 서브 쿼리 한계

- JPA는 WHERE, HAVING 절에서만 서브 쿼리 사용 가능
- SELECT 절도 가능(하이버네이트에서 지원)
- FROM 절의 서브 쿼리는 현재 JPQL에서 불가능
- 조인으로 풀 수 있으면 풀어서 해결


# 하이버네이트 6 변경 사항

- 하이버네이트 6 부터는 FROM 절의 서브쿼리를 지원 합니다.
- 참고 링크
- https://in.relation.to/2022/06/24/hibernate-orm-61-features/


# JPQL 타입 표현

- 문자: ‘HELLO’, ‘She’’s’
- 숫자: 10L(Long), 10D(Double), 10F(Float)
- Boolean: TRUE, FALSE
- ENUM: jpabook.MemberType.Admin (패키지명 포함)
- 엔티티 타입: TYPE(m) = Member (상속 관계에서 사용)


# JPQL 기타

- SQL과 문법이 같은 식
- EXISTS, IN
- AND, OR, NOT
- =, >, >=, <, <=, <>
- BETWEEN, LIKE, IS NULL


# 조건식 - CASE 식

- 기본 CASE 식
```
select
case when m.age <= 10 then '학생요금'
when m.age >= 60 then '경로요금'
else '일반요금'
end
from Member m
```

- 단순 CASE 식
```
select
case t.name
when '팀A' then '인센티브110%'
when '팀B' then '인센티브120%'
else '인센티브105%'
end
from Team t
```





# 조건식 - CASE 식

- COALESCE: 하나씩 조회해서 null이 아니면 반환
- NULLIF: 두 값이 같으면 null 반환, 다르면 첫번째 값 반환

사용자 이름이 ‘관리자’면 null을 반환하고 나머지는 본인의 이름을 반환

```
select coalesce (m.username,'이름 없는 회원') from Member m
```

사용자 이름이 없으면 이름 없는 회원을 반환

```
select NULLIF (m.username, '관리자') from Member m
```

# JPQL 기본 함수

- CONCAT
- SUBSTRING
- TRIM
- LOWER, UPPER
- LENGTH
- LOCATE
- ABS, SQRT, MOD
- SIZE, INDEX(JPA 용도)


# 사용자 정의 함수 호출

- 하이버네이트는 사용전 방언에 추가해야 한다.
- 사용하는 DB 방언을 상속받고, 사용자 정의 함수를 등록한다.

```
select function ('group_concat', i.name) from Item i
```

# JPQL - 경로 표현식


# 경로 표현식

- .(점)을 찍어 객체 그래프를 탐색하는 것

```
select m.username -> 상태 필드
from Member m
join m.team t -> 단일 값 연관 필드
join m.orders o -> 컬렉션 값 연관 필드
where t.name = '팀A'
```

# 경로 표현식 용어 정리

- **상태 필드** (state field): 단순히 값을 저장하기 위한 필드 (ex: m.username)
- **연관 필드** (association field): 연관관계를 위한 필드
  - **단일 값 연관 필드** :
    - @ManyToOne, @OneToOne, 대상이 엔티티(ex: m.team)
  - **컬렉션 값 연관 필드** :
    - @OneToMany, @ManyToMany, 대상이 컬렉션(ex: m.orders)


# 경로 표현식 특징

- **상태 필드** (state field): 경로 탐색의 끝, 탐색X
- **단일 값 연관 경로** : 묵시적 내부 조인(inner join) 발생, 탐색O
- **컬렉션 값 연관 경로** : 묵시적 내부 조인 발생, 탐색X
  - FROM 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 탐색 가능


# 상태 필드 경로 탐색

- JPQL: select m.username, m.age from Member m
- SQL: select m.username, m.age from Member m


# 단일 값 연관 경로 탐색

- JPQL: select **o.member** from Order o
- SQL: select m.* from Orders o **inner join Member m on o.member_id = m.id**


# 명시직 조인, 묵시적 조인

- 명시적 조인: join 키워드 직접 사용
  - select m from Member m **join m.team t**
- 묵시적 조인: 경로 표현식에 의해 묵시적으로 SQL 조인 발생 (내부 조인만 가능)
  - select **m.team** from Member m


# 경로 표현식 - 예제

- select o.member.team from Order o -> 성공
- select t.members from Team -> 성공
- select t.members.username from Team t -> 실패
- select m.username from Team t join t.members m -> 성공


# 경로 탐색을 사용한 묵시적 조인 시 주의사항

- 항상 내부 조인
- 컬렉션은 경로 탐색의 끝, 명시적 조인을 통해 별칭을 얻어야함
- 경로 탐색은 주로 SELECT, WHERE 절에서 사용하지만 묵시적 조인으로 인해 SQL의 FROM (JOIN) 절에 영향을 줌


# 실무 조언

- 가급적 묵시적 조인 대신에 명시적 조인 사용
- 조인은 SQL 튜닝에 중요 포인트
- 묵시적 조인은 조인이 일어나는 상황을 한눈에 파악하기 어려움


# JPQL - 페치 조인(fetch join)


**실무에서 정말정말 중요함**


# 페치 조인(fetch join)

- SQL 조인 종류X
- JPQL에서 성능 최적화 를 위해 제공하는 기능
- 연관된 엔티티나 컬렉션을 SQL 한 번에 함께 조회 하는 기능
- join fetch 명령어 사용
- 페치 조인 ::= [ LEFT [OUTER] | INNER ] JOIN FETCH 조인경로


# 엔티티 페치 조인

- 회원을 조회하면서 연관된 팀도 함께 조회(SQL 한 번에)

- SQL을 보면 회원 뿐만 아니라 팀(T.*) 도 함께 SELECT

- **[JPQL]** : select m from Member m join fetch m.team
- **[SQL]** : SELECT M.*, **T.*** FROM MEMBER M **INNER JOIN TEAM T** ON M.TEAM_ID=T.ID

![객체지향 쿼리 언어5](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/af67f93e-f665-41c0-953d-c129b1f2cc2a)

# 페치 조인 사용 코드

```java
String jpql = "select m from Member m join fetch m.team";
List<Member> members = em.createQuery(jpql, Member.class)
.getResultList();

for (Member member : members) {
//페치 조인으로 회원과 팀을 함께 조회해서 지연 로딩X
System.out.println("username = " + member.getUsername() + ", " +
"teamName = " + member.getTeam().name() );
}
```

```
username = 회원1, teamname = 팀A
username = 회원2, teamname = 팀A
username = 회원3, teamname = 팀B
```

# 컬렉션 페치 조인

- 일대다 관계, 컬렉션 페치 조인
- **[JPQL]** select t from Team t **join fetch t.members** where t.name = ‘팀A'
- **[SQL]** SELECT T.*, **M.** FROM TEAM T INNER JOIN MEMBER M ON T.ID=M.TEAM_ID WHERE T.NAME = '팀A'

![객체지향 쿼리 언어6](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/879dd1c4-b17c-4371-bb4a-ee920b36a0ba)

# 컬렉션 페치 조인 사용 코드

```java
String jpql = "select t from Team t join fetch t.members where t.name = '팀A'"
List<Team> teams = em.createQuery(jpql, Team.class).getResultList();

for(Team team : teams) {
System.out.println("teamname = " + team.getName() + ", team = " + team);
for (Member member : team.getMembers()) {
//페치 조인으로 팀과 회원을 함께 조회해서 지연 로딩 발생 안함
System.out.println(“-> username = " + **member.getUsername()** + ", member = " + member);
}
}
```
```
teamname = 팀A, team = Team@0x100
-> username = 회원1, member = Member@0x200
-> username = 회원2, member = Member@0x300
teamname = 팀A, team = Team@0x100
-> username = 회원1, member = Member@0x200
-> username = 회원2, member = Member@0x300
```

# 페치 조인과 DISTINCT

- SQL의 DISTINCT는 중복된 결과를 제거하는 명령
- JPQL의 DISTINCT 2가지 기능 제공
  - 1. SQL에 DISTINCT를 추가
  - 2. 애플리케이션에서 엔티티 중복 제거


# 페치 조인과 DISTINCT

![객체지향 쿼리 언어7](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/0919b759-5b61-431c-87d4-871e372122d4)

- select **distinct** t
  from Team t join fetch t.members
  where t.name = ‘팀A’
- SQL에 DISTINCT를 추가하지만 데이터가 다르므로 SQL 결과에서 중복제거 실패


# 페치 조인과 DISTINCT

![객체지향 쿼리 언어8](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/cadc4770-caa0-49a7-9b6c-155ed6bae2b6)

-  DISTINCT가 추가로 애플리케이션에서 중복 제거시도
- 같은 식별자를 가진 **Team 엔티티 제거**

```
[DISTINCT 추가시 결과]
teamname = 팀A, team = Team@0x100
-> username = 회원1, member = Member@0x200
-> username = 회원2, member = Member@0x300
```

# 하이버네이트 6 변경 사항

- DISTINCT가 추가로 애플리케이션에서 중복 제거시도
- -> 하이버네이트 6 부터는 DISTINCT 명령어를 사용하지 않아도 애플리케이션에서 중복 제거가 자동으로 적용됩니다.
- 참고 링크
- https://www.inflearn.com/questions/717679


# 페치 조인과 일반 조인의 차이

- 일반 조인 실행시 연관된 엔티티를 함께 조회하지 않음
- **[JPQL]**
  select t
  from Team t join t.members m
  where t.name = ‘팀A'
- **[SQL]**
  SELECT **T.***
  FROM TEAM T
  INNER JOIN MEMBER M ON T.ID=M.TEAM_ID
  WHERE T.NAME = '팀A'


# 페치 조인과 일반 조인의 차이

- JPQL은 결과를 반환할 때 연관관계 고려X
- 단지 SELECT 절에 지정한 엔티티만 조회할 뿐
- 여기서는 팀 엔티티만 조회하고, 회원 엔티티는 조회X


# 페치 조인과 일반 조인의 차이

- 페치 조인을 사용할 때만 연관된 엔티티도 함께 조회(즉시 로딩)
- 페치 조인은 객체 그래프를 SQL 한번에 조회하는 개념


# 페치 조인 실행 예시

- 페치 조인은 연관된 엔티티를 함께 조회함
- **[JPQL]**
  select t
  from Team t **join fetch** t.members
  where t.name = ‘팀A'
- **[SQL]**
  SELECT **T.*, M.***
  FROM TEAM T
  INNER JOIN MEMBER M ON T.ID=M.TEAM_ID
  WHERE T.NAME = '팀A'


# 페치 조인의 특징과 한계

- **페치 조인 대상에는 별칭을 줄 수 없다.**
  - 하이버네이트는 가능, 가급적 사용X
- **둘 이상의 컬렉션은 페치 조인 할 수 없다.**
- **컬렉션을 페치 조인하면 페이징 API(setFirstResult,**
  **setMaxResults)를 사용할 수 없다.**
  - 일대일, 다대일 같은 단일 값 연관 필드들은 페치 조인해도 페이징 가능
  - 하이버네이트는 경고 로그를 남기고 메모리에서 페이징(매우 위험)


# 페치 조인의 특징과 한계

- 연관된 엔티티들을 SQL 한 번으로 조회 - 성능 최적화
- 엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선함
- @OneToMany(fetch = FetchType.LAZY) //글로벌 로딩 전략
- 실무에서 글로벌 로딩 전략은 모두 지연 로딩
- 최적화가 필요한 곳은 페치 조인 적용


# 페치 조인 - 정리

- 모든 것을 페치 조인으로 해결할 수 는 없음
- 페치 조인은 객체 그래프를 유지할 때 사용하면 효과적
- 여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른
- 결과를 내야 하면, 페치 조인 보다는 일반 조인을 사용하고 필요
- 한 데이터들만 조회해서 DTO로 반환하는 것이 효과적


# JPQL - 다형성 쿼리

![객체지향 쿼리 언어9](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/3a1acde0-76a1-4e87-a6f7-06d1101854e2)

# TYPE

- 조회 대상을 특정 자식으로 한정
- 예) Item 중에 Book, Movie를 조회해라
- **[JPQL]**
  select i from Item i
  where **type(i)** IN (Book, Movie)
- **[SQL]**
  select i from i
  where i.DTYPE in (‘B’, ‘M’)


# TREAT(JPA 2.1)

- 자바의 타입 캐스팅과 유사
- 상속 구조에서 부모 타입을 특정 자식 타입으로 다룰 때 사용
- FROM, WHERE, SELECT(하이버네이트 지원) 사용


# TREAT(JPA 2.1)

- 예) 부모인 Item과 자식 Book이 있다.
- **[JPQL]**
  select i from Item i
  where treat(i as Book).author = ‘kim’
- **[SQL]**
  select i.* from Item i
  where i.DTYPE = ‘B’ and i.author = ‘kim’


# JPQL - 엔티티 직접 사용


# 엔티티 직접 사용 - 기본 키 값

- JPQL에서 엔티티를 직접 사용하면 SQL에서 해당 엔티티의 기
- 본 키 값을 사용
- **[JPQL]**
  select **count(m.id)** from Member m //엔티티의 아이디를 사용
  select **count(m)** from Member m //엔티티를 직접 사용
- **[SQL]** (JPQL 둘다 같은 다음 SQL 실행)
  select count(m.id) as cnt from Member m


# 엔티티 직접 사용 - 기본 키 값

엔티티를 파라미터로 전달
```java
String jpql = “select m from Member m where m = :member ”;
List resultList = em.createQuery(jpql)
.setParameter("member", member )
.getResultList();
```

식별자를 직접 전달
```java
String jpql = “select m from Member m where m.id = :memberId ”;
List resultList = em.createQuery(jpql)
.setParameter("memberId", memberId )
.getResultList();
```

실행된 SQL
```
select m.* from Member m where m.id=?
```

# 엔티티 직접 사용 - 외래 키 값

```java
Team team = em.find(Team.class, 1L);

String qlString = “select m from Member m where m.team = :team ”;
List resultList = em.createQuery(qlString)
.setParameter("team", team )
.getResultList();
```

```
String qlString = “select m from Member m where m.team.id = :teamId ”;
List resultList = em.createQuery(qlString)
.setParameter("teamId", teamId )
.getResultList();
```

**실행된 SQL**
```
select m.* from Member m where m.team_id =?
```

# JPQL - Named 쿼리


# Named 쿼리 - 정적 쿼리

- 미리 정의해서 이름을 부여해두고 사용하는 JPQL
- 정적 쿼리
- 어노테이션, XML에 정의
- 애플리케이션 로딩 시점에 초기화 후 재사용
- 애플리케이션 로딩 시점에 쿼리를 검증


# Named 쿼리 - 어노테이션

```java
@Entity
@NamedQuery (
name = "Member.findByUsername",
query="select m from Member m where m.username = :username")
public class Member {
...
}
```

```java
List<Member> resultList =
em.createNamedQuery("Member.findByUsername", Member.class)
.setParameter("username", "회원1")
.getResultList();
```

# Named 쿼리 - XML에 정의

![객체지향 쿼리 언어10](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/c85bc0ab-2c57-46f7-9250-8771a5a80c08)

# Named 쿼리 환경에 따른 설정

- XML이 항상 우선권을 가진다.
- 애플리케이션 운영 환경에 따라 다른 XML을 배포할 수 있다.


# JPQL - 벌크 연산


# 벌크 연산

- 재고가 10 개 미만인 모든 상품의 가격을 10% 상승하려면?
- JPA 변경 감지 기능으로 실행하려면 너무 많은 SQL 실행
  - 1. 재고가 10 개 미만인 상품을 리스트로 조회한다.
  - 2. 상품 엔티티의 가격을 10% 증가한다.
  - 3. 트랜잭션 커밋 시점에 변경감지가 동작한다.
- 변경된 데이터가 100 건이라면 100 번의 UPDATE SQL 실행


# 벌크 연산 예제

- 쿼리 한 번으로 여러 테이블 로우 변경(엔티티)
- **executeUpdate()의 결과는 영향받은 엔티티 수 반환**
- **UPDATE, DELETE 지원**
- **INSERT(insert into .. select, 하이버네이트 지원)**

```java
String qlString = "update Product p " +
"set p.price = p.price * 1.1 " +
"where p.stockAmount < :stockAmount";

int resultCount = em.createQuery(qlString)
.setParameter("stockAmount", 10)
.executeUpdate();
```

# 벌크 연산 주의

- 벌크 연산은 영속성 컨텍스트를 무시하고 데이터베이스에 직접 쿼리
  - 벌크 연산을 먼저 실행
  - 벌크 연산 수행 후 영속성 컨텍스트 초기화
