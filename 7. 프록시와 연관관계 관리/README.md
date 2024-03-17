# 7. 프록시와 연관관계 관리

## Index
- [프록시](#프록시)
  - [Member를 조회할 때 Team도 함께 조회해야 할까?](#member를-조회할-때-team도-함께-조회해야-할까)
  - [프록시 기초](#프록시-기초)
    - [em.getReference() 예제](#emgetreference-예제)
  - [프록시 특징](#프록시-특징)
  - [프록시 객체의 초기화](#프록시-객체의-초기화)
  - [프록시의 특징 (이게 중요하다!!!!)](#프록시의-특징-이게-중요하다)
  - [프록시 확인](#프록시-확인)
- [즉시 로딩과 지연 로딩](#즉시-로딩과-지연-로딩)
  - [Member를 조회할 때 Team도 함께 조회해야 할까? (즉시 로딩과 지연 로딩의 관점)](#member를-조회할-때-team도-함께-조회해야-할까-즉시-로딩과-지연-로딩의-관점)
  - [지연 로딩 LAZY을 사용해서 프록시로 조회](#지연-로딩-lazy을-사용해서-프록시로-조회)
  - [Member와 Team을 자주 함께 사용한다면?](#member와-team을-자주-함께-사용한다면)
  - [즉시 로딩 EAGER를 사용해서 함께 조회](#즉시-로딩-eager를-사용해서-함께-조회)
  - [즉시 로딩(EAGER), Member조회시 항상 Team도 조회](#즉시-로딩eager-member조회시-항상-team도-조회)
- [프록시와 즉시로딩 주의](#프록시와-즉시로딩-주의)
  - [N+1 문제 해결방법](#n1-문제-해결방법)
- [지연 로딩 활용](#지연-로딩-활용)
  - [위 내용들은 전부 이론적인거고 실무에서는 무조건 지연 로딩(FetchType.LAZY)으로 다 바르는게 좋다!!!!!](#위-내용들은-전부-이론적인거고-실무에서는-무조건-지연-로딩fetchtypelazy으로-다-바르는게-좋다)
  - [지연 로딩 활용 - 실무](#지연-로딩-활용---실무)
  - [모든 연관관계에 지연 로딩을 사용해라! 실무에서 즉시 로딩을 사용하지 마라!](#모든-연관관계에-지연-로딩을-사용해라-실무에서-즉시-로딩을-사용하지-마라)
- [영속성 전이: CASCADE](#영속성-전이-cascade)
  - [영속성 전이: 저장](#영속성-전이-저장)
  - [영속성 전이: CASCADE - 주의!](#영속성-전이-cascade---주의)
  - [CASCADE의 종류](#cascade의-종류)
- [고아 객체](#고아-객체)
  - [고아 객체 - 주의](#고아-객체---주의)
  - [영속성 전이 + 고아 객체, 생명주기](#영속성-전이--고아-객체-생명주기)


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

### Member를 조회할 때 Team도 함께 조회해야 할까? (즉시 로딩과 지연 로딩의 관점)

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


## 프록시와 즉시로딩 주의

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
