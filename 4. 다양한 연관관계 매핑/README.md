# 4. 다양한 연관관계 매핑

객체가 지향하는 패러다임과 관계형 DB 가 지향하는 패러다임의 불일치로 헷갈릴 수 있으니 주의해야 한다.

## 목표
- **객체와 테이블 연관관계의 차이를 이해**
- **객체의 참조와 테이블의 외래 키를 매핑**

용어 이해
- **방향(Direction)**: 단방향, 양방향
- **다중성(Multiplicity)**: 다대일(N:1), 일대다(1:N), 일대일(1:1), 다대다(N:M) 이해
- **연관관계의 주인(Owner)**: 객체 양방향 연관관계는 관리 주인이 필요

## Index
- [연관관계가 필요한 이유](#연관관계가-필요한-이유)
  - [객체를 테이블에 맞추어 모델링 (연관관계가 없는 객체)](#객체를-테이블에-맞추어-모델링-연관관계가-없는-객체)
- [단방향 연관관계](#단방향-연관관계)
  - [객체 지향 모델링](#객체-지향-모델링)
- [양방향 연관관계와 연관관계의 주인](#양방향-연관관계와-연관관계의-주인)
  - [양방향 매핑](#양방향-매핑)
    - [연관관계의 주인과 mappedBy](#연관관계의-주인과-mappedby)
    - [객체와 테이블이 관계를 맺는 차이](#객체와-테이블이-관계를-맺는-차이)
    - [객체의 양방향 관계](#객체의-양방향-관계)
    - [테이블의 양방향 연관관계](#테이블의-양방향-연관관계)
    - [둘 중 하나로 외래 키를 관리해야 한다.](#둘-중-하나로-외래-키를-관리해야-한다)
- [연관관계의 주인(Owner)](#연관관계의-주인owner)
  - [누구를 주인으로? - 외래 키가 있는 있는 곳을 주인](#누구를-주인으로---외래-키가-있는-있는-곳을-주인)
    - [양방향 매핑시 가장 많이 하는 실수 (연관관계의 주인에 값을 입력하지 않음)](#양방향-매핑시-가장-많이-하는-실수-연관관계의-주인에-값을-입력하지-않음)
    - [양방향 매핑시 연관관계의 주인에 값을 입력해야 한다. (순수한 객체 관계를 고려하면 항상 양쪽 다 값을 입력해야 한다.)](#양방향-매핑시-연관관계의-주인에-값을-입력해야-한다-순수한-객체-관계를-고려하면-항상-양쪽-다-값을-입력해야-한다)
    - [양방향 연관관계 주의 - 실습](#양방향-연관관계-주의---실습)
    - [양방향 매핑 정리](#양방향-매핑-정리)
    - [연관관계의 주인을 정하는 기준](#연관관계의-주인을-정하는-기준)
- [실전 예제 - 2. 연관관계 매핑 시작](#실전-예제---2-연관관계-매핑-시작)

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
