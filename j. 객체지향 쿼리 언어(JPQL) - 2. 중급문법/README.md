# 10. 객체지향 쿼리 언어(JPQL) - 2. 중급문법

## Index
- [JPQL - 경로 표현식](#jpql---경로-표현식)
  - [경로 표현식](#경로-표현식)
  - [경로 표현식 용어 정리: 상태 필드, 단일 값 연관 필드, 컬렉션 값 연관 필드](#경로-표현식-용어-정리-상태-필드-단일-값-연관-필드-컬렉션-값-연관-필드)
  - [경로 표현식 특징](#경로-표현식-특징)
  - [상태 필드 경로 탐색](#상태-필드-경로-탐색)
  - [단일 값 연관 경로 탐색](#단일-값-연관-경로-탐색)
  - [명시직 조인, 묵시적 조인](#명시직-조인-묵시적-조인)
  - [경로 표현식 - 예제](#경로-표현식---예제)
  - [경로 탐색을 사용한 묵시적 조인 시 주의사항](#경로-탐색을-사용한-묵시적-조인-시-주의사항)
  - [조인에 관한 실무 조언](#조인에-관한-실무-조언)
- [JPQL - 페치 조인(fetch join)](#jpql---페치-조인fetch-join)
  - [페치 조인(fetch join)](#페치-조인fetch-join)
  - [엔티티 페치 조인](#엔티티-페치-조인)
    - [페치 조인 사용 코드](#페치-조인-사용-코드)
  - [컬렉션 페치 조인](#컬렉션-페치-조인)
    - [컬렉션 페치 조인 사용 코드](#컬렉션-페치-조인-사용-코드)
  - [페치 조인과 DISTINCT](#페치-조인과-distinct)
  - [하이버네이트 6 변경 사항](#하이버네이트-6-변경-사항)
- [페치 조인과 일반 조인의 차이](#페치-조인과-일반-조인의-차이)
  - [즉시 로딩(EAGER)과 패치조인](#즉시-로딩eager과-패치조인)
- [페치 조인의 특징과 한계](#페치-조인의-특징과-한계)
  - [일대다 관계 페치 조인 후 페이징 API 사용 방법](#일대다-관계-페치-조인-후-페이징-api-사용-방법)
  - [페치 조인의 특징과 한계 다시 한번 정리](#페치-조인의-특징과-한계-다시-한번-정리)
  - [페치 조인 - 정리](#페치-조인---정리)
- [JPQL - 다형성 쿼리](#jpql---다형성-쿼리)
  - [TYPE](#type)
  - [TREAT(JPA 2.1)](#treatjpa-21)
- [JPQL - 엔티티 직접 사용](#jpql---엔티티-직접-사용)
  - [엔티티 직접 사용 - 기본 키 값](#엔티티-직접-사용---기본-키-값)
  - [엔티티 직접 사용 - 기본 키 값 예시](#엔티티-직접-사용---기본-키-값-예시)
  - [엔티티 직접 사용 - 외래 키 값](#엔티티-직접-사용---외래-키-값)
- [JPQL - Named 쿼리](#jpql---named-쿼리)
  - [Named 쿼리 - 정적 쿼리](#named-쿼리---정적-쿼리)
  - [Named 쿼리 - 어노테이션](#named-쿼리---어노테이션)
  - [Named 쿼리 - XML에 정의](#named-쿼리---xml에-정의)
  - [Named 쿼리 환경에 따른 설정](#named-쿼리-환경에-따른-설정)
  - [Spring Data JPA 의 @Query](#spring-data-jpa-의-query)
- [JPQL - 벌크 연산](#jpql---벌크-연산)
  - [벌크 연산](#벌크-연산)
  - [벌크 연산 예제](#벌크-연산-예제)
  - [벌크 연산 주의](#벌크-연산-주의)
  - [벌크 연산 주의점 예시](#벌크-연산-주의점-예시)



## JPQL - 경로 표현식


### 경로 표현식

- .(점)을 찍어 객체 그래프를 탐색하는 것

```sql
select m.username -> 상태 필드
    from Member m
        join m.team t -> 단일 값 연관 필드
        join m.orders o -> 컬렉션 값 연관 필드
where t.name = '팀A'
```

### 경로 표현식 용어 정리: 상태 필드, 단일 값 연관 필드, 컬렉션 값 연관 필드

- **상태 필드** (state field): 단순히 값을 저장하기 위한 필드 (ex: m.username)
- **연관 필드** (association field): 연관관계를 위한 필드
  - **단일 값 연관 필드** :
    - @ManyToOne, @OneToOne, 대상이 엔티티(ex: m.team)
  - **컬렉션 값 연관 필드** :
    - @OneToMany, @ManyToMany, 대상이 컬렉션(ex: m.orders)


### 경로 표현식 특징

- **상태 필드** (state field): 경로 탐색의 끝, 탐색X
- **단일 값 연관 경로** : 묵시적 내부 조인(inner join) 발생, 탐색O
- **컬렉션 값 연관 경로** : 묵시적 내부 조인(inner join) 발생, 탐색X
  - FROM 절에서 명시적 조인을 통해 별칭을 얻으면 별칭을 통해 탐색 가능

***쿼리 튜닝할 때 어렵다.***

***조인은 선능 튜닝할 때 큰 영향을 미친다.***

***웬만하면 묵시적 내부 조인이 발생하도록 짜면 안된다!!!***

### 상태 필드 경로 탐색

- JPQL: select m.username, m.age from Member m
- SQL: select m.username, m.age from Member m


### 단일 값 연관 경로 탐색

- JPQL: select **o.member** from Order o
- SQL: select m.* from Orders o **inner join Member m on o.member_id = m.id**


### 명시직 조인, 묵시적 조인

- **명시적 조인**: join 키워드 직접 사용
  - select m from Member m **join m.team t**
- **묵시적 조인**: 경로 표현식에 의해 묵시적으로 SQL 조인 발생 (내부 조인만 가능)
  - select **m.team** from Member m


### 경로 표현식 - 예제

- `select o.member.team from Order o` -> 성공

```
Hibernate: 
    /* select
        o.member.team 
    from
        
    Order o */ select
        t1_0.id,
        t1_0.name,
        t1_0.orderAmount from
            ORDERS o1_0 
        join
            Member m1_0 
                on m1_0.id=o1_0.MEMBER_ID 
        join
            Team t1_0 
                on t1_0.id=m1_0.TEAM_ID
```
- `select t.members from Team` -> 성공

```
Hibernate: 
    /* select
        t.members 
    from
        Team t */ select
            m1_0.id,
            m1_0.age,
            t1_0.id,
            t1_0.name,
            t1_0.orderAmount,
            m1_0.type,
            m1_0.username 
        from
            Team t1_0 
        join
            Member m1_0 
                on t1_0.id=m1_0.TEAM_ID
```

- select t.members.username from Team t -> 실패
- `select m.username from Team t join t.members m` -> 성공

```
Hibernate: 
    /* select
        m.username 
    from
        Team t 
    join
        t.members m */ select
            m1_0.username 
        from
            Team t1_0 
        join
            Member m1_0 
                on t1_0.id=m1_0.TEAM_ID
```


### 경로 탐색을 사용한 묵시적 조인 시 주의사항

- 항상 내부 조인
- 컬렉션은 경로 탐색의 끝, 명시적 조인을 통해 별칭을 얻어야함
- 경로 탐색은 주로 SELECT, WHERE 절에서 사용하지만 묵시적 조인으로 인해 SQL의 FROM (JOIN) 절에 영향을 줌


### 조인에 관한 실무 조언

- ***가급적 묵시적 조인 대신에 명시적 조인 사용***
- 조인은 SQL 튜닝에 중요 포인트
- 묵시적 조인은 조인이 일어나는 상황을 한눈에 파악하기 어려움


## JPQL - 페치 조인(fetch join)


***실무에서 정말정말 중요함***


### 페치 조인(fetch join)

- SQL 조인 종류X
- **JPQL에서 성능 최적화를 위해 제공하는 기능**
- 연관된 엔티티나 컬렉션을 SQL 한 번에 함께 조회 하는 기능
- join fetch 명령어 사용
- 페치 조인 ::= [ LEFT [OUTER] | INNER ] JOIN FETCH 조인경로


### 엔티티 페치 조인

- 회원을 조회하면서 연관된 팀도 함께 조회(SQL 한 번에)

- SQL을 보면 회원 뿐만 아니라 팀(T.*) 도 함께 SELECT

- **[JPQL]** : `select m from Member m join fetch m.team`
- **[SQL]** : `SELECT M.*, T.* FROM MEMBER M INNER JOIN TEAM T ON M.TEAM_ID=T.ID`

![객체지향 쿼리 언어5](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/af67f93e-f665-41c0-953d-c129b1f2cc2a)

#### 페치 조인 사용 코드

```java
public static void main(String[] args) {
  Team teamA = new Team();
  teamA.setName("teamA");
  em.persist(teamA);

  Team teamB = new Team();
  teamB.setName("teamB");
  em.persist(teamB);

  Team teamC = new Team();
  teamC.setName("teamB");
  em.persist(teamC);

  Member member1 = new Member();
  member1.setUsername("member1");
  member1.setAge(22);
  member1.changeTeam(teamA);
  em.persist(member1);

  Member member2 = new Member();
  member2.setUsername("member2");
  member2.setAge(22);
  member2.changeTeam(teamA);
  em.persist(member2);

  Member member3 = new Member();
  member3.setUsername("member3");
  member3.setAge(22);
  member3.changeTeam(teamB);
  em.persist(member3);

  Member member4 = new Member();
  member4.setUsername("member4");
  member4.setAge(22);
  em.persist(member4);

  em.flush();
  em.clear();

  String query = "select m from Member m join fetch m.team";
  List<Member> result = em.createQuery(query).getResultList();

  for (Member member : result) {
    System.out.println("member = " + member.getUsername() + ", " + member.getTeam().getName());
  }
}
```

```
Hibernate: 
    /* select
        m 
    from
        Member m 
    join
        
    fetch
        m.team t */ select
            m1_0.id,
            m1_0.age,
            t1_0.id,
            t1_0.name,
            m1_0.type,
            m1_0.username 
        from
            Member m1_0 
        join
            Team t1_0 
                on t1_0.id=m1_0.TEAM_ID
member = member1, teamA
member = member2, teamA
member = member3, teamB
```

### 컬렉션 페치 조인

- 일대다 관계, 컬렉션 페치 조인
- **[JPQL]** select t from Team t **join fetch t.members** where t.name = ‘팀A'
- **[SQL]** SELECT T.*, **M.** FROM TEAM T INNER JOIN MEMBER M ON T.ID=M.TEAM_ID WHERE T.NAME = '팀A'

![객체지향 쿼리 언어6](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/879dd1c4-b17c-4371-bb4a-ee920b36a0ba)

#### 컬렉션 페치 조인 사용 코드

**하이버네이트6 부터는 distinct 명령어를 사용하지 않아도 엔티티의 중복을 제거하도록 변경되었습니다.**

```java
public static void main(String[] args) {
  String query = "select t from Team t join fetch t.members";
  List<Team> result = em.createQuery(query, Team.class).getResultList();

  System.out.println("result.size() = " + result.size());

  for (Team t : result) {
    System.out.println("team = " + t.getName());
    for (Member m : t.getMembers()) {
      System.out.println("-> member = " + m.getUsername());
    }
  }
}
```

```
Hibernate: 
    /* select
        t 
    from
        Team t 
    join
        
    fetch
        t.members m */ select
            t1_0.id,
            m1_0.TEAM_ID,
            m1_0.id,
            m1_0.age,
            m1_0.type,
            m1_0.username,
            t1_0.name 
        from
            Team t1_0 
        join
            Member m1_0 
                on t1_0.id=m1_0.TEAM_ID
result.size() = 2
team = teamA
-> member = member1
-> member = member2
team = teamB
-> member = member3
```

### 페치 조인과 DISTINCT

- SQL의 DISTINCT는 중복된 결과를 제거하는 명령
- JPQL의 DISTINCT 2가지 기능 제공
  - 1. **SQL에 DISTINCT를 추가**
  - 2. **애플리케이션에서 엔티티 중복 제거**

![객체지향 쿼리 언어7](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/0919b759-5b61-431c-87d4-871e372122d4)

```sql
select distinct t
from Team t join fetch t.members
where t.name = '팀A'
```

- SQL에 DISTINCT를 추가하지만 데이터가 다르므로 **SQL 결과에서** 중복제거 실패

![객체지향 쿼리 언어8](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/cadc4770-caa0-49a7-9b6c-155ed6bae2b6)

- DISTINCT 가 추가로 **애플리케이션에서** 중복 제거시도
- 같은 식별자를 가진 **Team 엔티티 제거**

```
[DISTINCT 추가시 결과]
Hibernate: 
    /* select
        distinct t 
    from
        Team t 
    join
        
    fetch
        t.members */ select
            distinct t1_0.id,
            m1_0.TEAM_ID,
            m1_0.id,
            m1_0.age,
            m1_0.type,
            m1_0.username,
            t1_0.name 
        from
            Team t1_0 
        join
            Member m1_0 
                on t1_0.id=m1_0.TEAM_ID
result.size() = 2
team = teamA
-> member = member1
-> member = member2
team = teamB
-> member = member3
```

### 하이버네이트 6 변경 사항

- DISTINCT가 추가로 애플리케이션에서 중복 제거시도
- -> 하이버네이트 6 부터는 DISTINCT 명령어를 사용하지 않아도 애플리케이션에서 중복 제거가 자동으로 적용됩니다.
- 참고 링크
- https://www.inflearn.com/questions/717679


## 페치 조인과 일반 조인의 차이

<img width="665" alt="스크린샷 2024-03-15 오전 11 23 06" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/71de7e22-408a-4bcd-943e-5fb004fc977c">

- **em.find()로 엔티티를 직접 조회하는 부분은 빼고, JPQL을 했을 경우로 한정하면 정확합니다.**

***일반 조인 실행시 연관된 엔티티를 함께 조회하지 않음***

- **[JPQL]**
  `select t
  from Team t join t.members m
  where t.name = '팀A'`
- **[SQL]**
  `SELECT T.*
  FROM TEAM T
  INNER JOIN MEMBER M ON T.ID=M.TEAM_ID
  WHERE T.NAME = '팀A'`


***페치 조인은 연관된 엔티티를 함께 조회함***

- **[JPQL]**
  `select t
  from Team t **join fetch** t.members
  where t.name = '팀A'`
- **[SQL]**
  `SELECT **T.*, M.***
  FROM TEAM T
  INNER JOIN MEMBER M ON T.ID=M.TEAM_ID
  WHERE T.NAME = '팀A'`


- JPQL은 결과를 반환할 때 연관관계 고려X, 단지 SELECT 절에 지정한 엔티티만 조회할 뿐
- 여기서는 팀 엔티티만 조회하고, 회원 엔티티는 조회X
- 페치 조인을 사용할 때만 연관된 엔티티도 함께 조회(즉시 로딩)
- **페치 조인은 *객체 그래프*를 SQL 한번에 조회하는 개념**

### 즉시 로딩(EAGER)과 패치조인

em.find() 등을 통해서 엔티티 하나만 조회할 때는 즉시 로딩으로 설정하면 연관된 팀도 한 쿼리로 가져오도록 최적화 되지만 JPQL을 사용하면 이야기가 달라집니다. JPQL은 연관관계를 즉시로딩으로 설정하는 것과 상관없이 JPQL 자체만으로 SQL로 그대로 번역됩니다.

## 페치 조인의 특징과 한계

1. **페치 조인 대상에는 별칭을 줄 수 없다.**
- 하이버네이트는 가능, 가급적 사용X
2. **둘 이상의 컬렉션은 페치 조인 할 수 없다.**
3. **컬렉션을 페치 조인하면 페이징 API(setFirstResult, setMaxResults)를 사용할 수 없다.**
- **일대다 관계는 데이터 뻥튀기가 일어나기 떄문에 사용할 수 없다!!!**
  - 일대일, 다대일 같은 단일 값 연관 필드들은 페치 조인해도 페이징 가능
  - 하이버네이트는 경고 로그를 남기고 메모리에서 페이징(매우 위험)

컬렉션으로 들어갔을 때 특정한 데이터만 나올 수 있는 것이 아닌 데이터 전부가 다 나와야 한다. 그것이 객체 그래프의 설계이념이다.

### 일대다 관계 페치 조인 후 페이징 API 사용 방법

**1. 방향을 뒤집어서 해결**: 쿼리 바꾸기 일대다는 다대일로 바꿀 수 있다.
- `select t from Team t join fetch t.members` -> `select m from Members m join fetch m.team`
**2. `@BatchSize` 사용**
- BatchSize 의 크기는 1000 이하의 크기로 적당히 크게 준다.
- `<property name="hibernate.default_batch_fetch_size" value="100"/>` 이렇게 값을 설정할 수도 있다.
- Team 을 가져올 때, Member 는 lazy loading 상태이다.
- lazy loading 상태인 놈을 갖고 올 때,
- `select t from Team t` 에서 나온 team 의 갯수만큼 `where m1_0.TEAM_ID in(?,?)` 의 in 절에 TEAM_ID 값을 넣어준다.
- 만약에 150개가 있다면, 처음에 in (?,?..) 에 물음표(TEAM_ID 값)를 100개를 날리고, 두번째는 남은 50개를 날린다.
**3. DTO 를 만들어서 사용**
- 이 방법도 비슷하게 정제를 해줘야 하기 때문에 만만치가 않다.

```java
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import org.hibernate.annotations.BatchSize;

@Entity
public class Team {
  ...
  @BatchSize(size = 100)
  @OneToMany(mappedBy ="team")
  private List<Member> members = new ArrayList<>();
  ...
}

public static void main(String[] args) {
  String query = "select t from Team t";
  List<Team> result = em.createQuery(query, Team.class)
    .setFirstResult(0)
    .setMaxResults(2)
    .getResultList();

  System.out.println("result.size() = " + result.size());

  for (Team t : result) {
    System.out.println("team = " + t.getName());
    for (Member m : t.getMembers()) {
      System.out.println("-> member = " + m.getUsername());
    }
  }
}
```

```
Hibernate: 
    /* select
        t 
    from
        Team t */ select
            t1_0.id,
            t1_0.name 
        from
            Team t1_0 offset ? rows fetch first ? rows only
result.size() = 2
team = teamA
Hibernate: 
    select
        m1_0.TEAM_ID,
        m1_0.id,
        m1_0.age,
        m1_0.type,
        m1_0.username 
    from
        Member m1_0 
    where
        m1_0.TEAM_ID in(?,?)
-> member = member1
-> member = member2
team = teamB
-> member = member3
```

### 페치 조인의 특징과 한계 다시 한번 정리

- 연관된 엔티티들을 SQL 한 번으로 조회 - 성능 최적화
- 엔티티에 직접 적용하는 글로벌 로딩 전략보다 우선함
- @OneToMany(fetch = FetchType.LAZY) //글로벌 로딩 전략
- 실무에서 글로벌 로딩 전략은 모두 지연 로딩
- 최적화가 필요한 곳은 페치 조인 적용


### 페치 조인 - 정리

- 모든 것을 페치 조인으로 해결할 수 는 없음
- 페치 조인은 객체 그래프를 유지할 때 사용하면 효과적
- 여러 테이블을 조인해서 엔티티가 가진 모양이 아닌 전혀 다른 결과를 내야 하면, 페치 조인 보다는 일반 조인을 사용하고 필요한 데이터들만 조회해서 DTO로 반환하는 것이 효과적

**데이터 조회시 사용하는 일반적인 3가지 방법**
> **1. 패치 조인 같은 것을 사용해서 엔티티를 조회해온다. 그것을 그대로 쓴다.**

> **2. 패치 조인 같은 것을 사용해서 조회해온 엔티티 데이터를 애플리케인션에서 DTO 로 변환해서 사용한다.**

> **3. 그냥 처음부터 jpql 작성할 때 부터 new operation 으로 DTO 로 변환해서 가져온다.**


## JPQL - 다형성 쿼리

![객체지향 쿼리 언어9](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/3a1acde0-76a1-4e87-a6f7-06d1101854e2)

### TYPE

- 조회 대상을 특정 자식으로 한정
- 예) Item 중에 Book, Movie를 조회해라
  - **`type(i)`**
- **[JPQL]**
  - `select i from Item i where type(i) IN (Book, Movie)`
- **[SQL]**
  - `select i from i where i.DTYPE in ('B', 'M')`


### TREAT(JPA 2.1)

- 자바의 타입 캐스팅과 유사
- 상속 구조에서 부모 타입을 특정 자식 타입으로 다룰 때 사용
- FROM, WHERE, SELECT(하이버네이트 지원) 사용
- 예) 부모인 Item과 자식 Book이 있다.
  - **`treat(i as Book)`**
- 아래의 쿼리는 `InheritanceType.SINGLE_TABLE` (싱글테이블) 전략을 예시로 쿼리가 나가는 것이다.
- **[JPQL]**
  - `select i from Item i where treat(i as Book).author = 'kim'`
- **[SQL]**
  - `select i.* from Item i where i.DTYPE = ‘B’ and i.author = 'kim'`


## JPQL - 엔티티 직접 사용


### 엔티티 직접 사용 - 기본 키 값

- JPQL에서 엔티티(ex: count(m))를 직접 사용하면 SQL에서 해당 **엔티티의 기본 키 값**을 사용
- **[JPQL]**
  - select **count(m.id)** from Member m //엔티티의 아이디를 사용
  - select **count(m)** from Member m //엔티티를 직접 사용
- **[SQL]** (JPQL 둘다 같은 다음 SQL 실행)
  select count(m.id) as cnt from Member m


### 엔티티 직접 사용 - 기본 키 값 예시

- 엔티티를 **파라미터(`where m =:member`)로** 전달
```java
public static void main(String[] args) {
  String query = "select m from Member m where m =:member";
  Member findMember = em.createQuery(query, Member.class)
    .setParameter("member", member2)
    .getSingleResult();

  System.out.println("findMember = " + findMember);
}
```

```sql
Hibernate: 
    /* select
        m 
    from
        Member m 
    where
        m =:member */ select
            m1_0.id,
            m1_0.age,
            m1_0.TEAM_ID,
            m1_0.type,
            m1_0.username 
        from
            Member m1_0 
        where
            m1_0.id=?
findMember = Member{id=2, username='member2', age=22, type=null}
```

- 식별자를 **직접 전달 (`where m.id =:memberId`)**
```java
public static void main(String[] args) {
  String query = "select m from Member m where m.id =:memberId";
  Member findMember = em.createQuery(query, Member.class)
    .setParameter("memberId", member2.getId())
    .getSingleResult();

  System.out.println("findMember = " + findMember);
}
```

```sql
Hibernate: 
    /* select
        m 
    from
        Member m 
    where
        m.id =:memberId */ select
            m1_0.id,
            m1_0.age,
            m1_0.TEAM_ID,
            m1_0.type,
            m1_0.username 
        from
            Member m1_0 
        where
            m1_0.id=?
findMember = Member{id=2, username='member2', age=22, type=null}
```


- 실행된 SQL
  - `where m1_0.id=?`
```sql
select m.* from Member m where m.id=?
```

### 엔티티 직접 사용 - 외래 키 값

- entity 를 파라미터로 전달
```java
public static void main(String[] args) {
  String query = "select m from Member m where m.team = :team";
  List<Member> result = em.createQuery(query, Member.class)
    .setParameter("team", teamA)
    .getResultList();

  for (Member member : result) {
    System.out.println("member = " + member);
  }
}
```

```sql
Hibernate: 
    /* select
        m 
    from
        Member m 
    where
        m.team = :team */ select
            m1_0.id,
            m1_0.age,
            m1_0.TEAM_ID,
            m1_0.type,
            m1_0.username 
        from
            Member m1_0 
        where
            m1_0.TEAM_ID=?
member = Member{id=1, username='member1', age=22, type=null}
member = Member{id=2, username='member2', age=22, type=null}
```

- 식별자를 전달
```java
public static void main(String[] args) {
  String query = "select m from Member m where m.team.id = :teamId";
  List<Member> result = em.createQuery(query, Member.class)
    .setParameter("teamId", teamA.getId())
    .getResultList();

  for (Member member : result) {
    System.out.println("member = " + member);
  }
}
```

```sql
Hibernate: 
    /* select
        m 
    from
        Member m 
    where
        m.team.id = :teamId */ select
            m1_0.id,
            m1_0.age,
            m1_0.TEAM_ID,
            m1_0.type,
            m1_0.username 
        from
            Member m1_0 
        where
            m1_0.TEAM_ID=?
member = Member{id=1, username='member1', age=22, type=null}
member = Member{id=2, username='member2', age=22, type=null}
```

**실행된 SQL**
- `m1_0.TEAM_ID=?`
```sql
select m1_0.id,
            m1_0.age,
            m1_0.TEAM_ID,
            m1_0.type,
            m1_0.username 
        from
            Member m1_0 
        where
            m1_0.TEAM_ID=?
```

## JPQL - Named 쿼리

### Named 쿼리 - 정적 쿼리

**미리 정의해서 이름을 부여해두고 사용하는 JPQL**
- 말 그대로 query 에 이름을 부여하는 것
- 이름으로 query 를 불러와서 처리를 할 수 있다.
  - 사실 어마어마한 메리트가 있다.
- 정적 쿼리
- 어노테이션, XML에 정의

**애플리케이션 로딩 시점에 초기화 후 재사용, 애플리케이션 로딩 시점에 쿼리를 검증**
- query 는 변하지 않는다.
- 애플리케이션 로딩 시점에 JPA, Hibernate 같은 애들이 이 query 를 sql 로 파싱한다.
- 그 다음에 **캐싱을 하고 저장하기 때문에 cost 가 거의 없다.**

### Named 쿼리 - 어노테이션

```java
@Entity
@NamedQuery(
  name = "Member.findByUsername",
  query = "select m from Member m where m.username = :username"
)
public class Member {
...
}

public static void main(String[] args) {
  List<Member> members = em.createNamedQuery("Member.findByUsername", Member.class)
    .setParameter("username", member3.getUsername())
    .getResultList();

  for (Member member : members) {
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
    where
        m.username = :username */ select
            m1_0.id,
            m1_0.age,
            m1_0.TEAM_ID,
            m1_0.type,
            m1_0.username 
        from
            Member m1_0 
        where
            m1_0.username=?
member = Member{id=3, username='member3', age=22, type=null}
```

예상했던 데로 query 는 잘 나간다.

```java
@NamedQuery(
  name = "Member.findByUsername",
  query = "select m from Members m where m.username = :username"
)
```

만약 위와 같이 query 에 문제가 있어도 문자열이기 때문에 문제를 발견하지 못하고 애플리케이션을 실행할 수도 있지만, **어노테이션에 등록이 되어 있기 때문에 애플리케이션 로딩 시점에 JPA 를 올리면서 jpql 을 sql 로 파싱하면서 캐싱을 해놓기 위해서 쿼리를 검증하기 때문에 이때 query 가 잘못 작성되어 있으면, 에러를 발생시켜준다. - 컴파일 에러를 발생**

```
Caused by: org.hibernate.HibernateException: Errors in named queries: Member.findByUsername
  Suppressed: org.hibernate.query.sqm.UnknownEntityException: Could not resolve root entity 'Members'
```

### Named 쿼리 - XML에 정의

![객체지향 쿼리 언어10](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/c85bc0ab-2c57-46f7-9250-8771a5a80c08)

### Named 쿼리 환경에 따른 설정

- XML이 항상 우선권을 가진다.
- 애플리케이션 운영 환경에 따라 다른 XML을 배포할 수 있다.
  - 특정 상황마다 쿼리가 다르게 나가야 할 때, 쿼리도 그에 따라 달라질 수 있다. 그럴 때, 매핑 파일을 따로 배포하면 된다.

### Spring Data JPA 의 @Query

**아래의 `@Query` 어노테이션이 `@NamedQuery` 어노테이션이다. JPA 가 이것을 NamedQuery 로 등록하는 것이다.**

```java
public interface UserRepository implements JpaRepository<User, Long> {
  @Query(select u from User u where u.name = :username)
  public findByUsername(String username)
}
```

## JPQL - 벌크 연산

### 벌크 연산

> 벌크연산이란? PK를 통해 데이터 한건을 변경하는 것을 제외한 나머지 모든 update, delete 문을 의미. 여러건을 변경하는 것

**EX) 벌크 연산을 사용하지 않았을 때의 예시**
- 재고가 10 개 미만인 모든 상품의 가격을 10% 상승하려면?
- JPA 변경 감지 기능으로 실행하려면 너무 많은 SQL 실행
  - 1. 재고가 10 개 미만인 상품을 리스트로 조회한다.
  - 2. 상품 엔티티의 가격을 10% 증가한다.
  - 3. 트랜잭션 커밋 시점에 변경감지가 동작한다.

**EX) 벌크 연산을 사용할 때**
- 변경된 데이터가 100 건이라면 100 번의 UPDATE SQL 실행


### 벌크 연산 예제

- 쿼리 한 번으로 여러 테이블 로우 변경(엔티티)
- **executeUpdate()의 결과는 영향받은 엔티티 수 반환**
- **UPDATE, DELETE 지원**
- **INSERT(insert into .. select, 하이버네이트 지원)**

```java
public static void main(String[] args) {
  // flush() 자동 호출된다. flush() 는 commit 이 될 때, query 가 나갈 때, flush() 를 직접 호출할 때 호출된다. 
  int count = em.createQuery("update Member m set age = 10")
    .executeUpdate();

  System.out.println("count = " + count);
}
```

```
Hibernate: 
    /* update
        Member m 
    set
        age = 10 */ update Member 
    set
        age=10
count = 4
```

```java
public static void main(String[] args) {
  String qlString = "update Product p " +
    "set p.price = p.price * 1.1 " +
    "where p.stockAmount < :stockAmount";

  int resultCount = em.createQuery(qlString)
    .setParameter("stockAmount", 10)
    .executeUpdate();
}
```

### 벌크 연산 주의

- 벌크 연산은 영속성 컨텍스트를 무시하고 데이터베이스에 직접 쿼리
  - 벌크 연산도 flush 는 된다. jpql 이 실행되고 sql 로 변형해서 쿼리가 나가는 것이기 때문에
    - flush() 는 commit 이 될 때, query 가 나갈 때, flush() 를 직접 호출할 때 호출된다.

**벌크 연산을 사용하는 2가지 방법**
1. 영속성 컨텍스트에 값을 아무것도 넣지 않은 상태에서 **벌크 연산을 먼저 실행**
2. 영속성 컨텍스트 안에 다른 값들이 이미 존재해서 불안할 떄, **벌크 연산 수행 후 영속성 컨텍스트 초기화한다.**


### 벌크 연산 주의점 예시

모든 Member 의 age 를 10 으로 변경해서 db 에는 10 으로 변경한 것이 확인되지만, 영속성 컨텍스트안의 member 들은 그대로 age 가 변경되기 전으로 나온다.

```java
public static void main(String[] args) {
  Team teamA = new Team();
  teamA.setName("teamA");
  em.persist(teamA);

  Team teamB = new Team();
  teamB.setName("teamB");
  em.persist(teamB);

  Team teamC = new Team();
  teamC.setName("teamB");
  em.persist(teamC);

  Member member1 = new Member();
  member1.setUsername("member1");
  member1.setAge(22);
  member1.changeTeam(teamA);
  em.persist(member1);

  Member member2 = new Member();
  member2.setUsername("member2");
  member2.setAge(22);
  member2.changeTeam(teamA);
  em.persist(member2);

  Member member3 = new Member();
  member3.setUsername("member3");
  member3.setAge(22);
  member3.changeTeam(teamB);
  em.persist(member3);

  Member member4 = new Member();
  member4.setUsername("member4");
  member4.setAge(22);
  em.persist(member4);

  int count = em.createQuery("update Member m set age = 10")
    .executeUpdate();

  System.out.println("count = " + count);
  System.out.println("member1.getAge() = " + member1.getAge());
  System.out.println("member1.getAge() = " + member2.getAge());
  System.out.println("member1.getAge() = " + member3.getAge());
  System.out.println("member1.getAge() = " + member4.getAge());
}
```

```
Hibernate: 
    /* update
        Member m 
    set
        age = 10 */ update Member 
    set
        age=10
count = 4
member1.getAge() = 22
member1.getAge() = 22
member1.getAge() = 22
member1.getAge() = 22
```

이때 em.find() 로 새로 조회해와도 똑같다. 똑같이 update 문이 반영이 안된 age 가 나온다.

```java
public static void main(String[] args) {
  int count = em.createQuery("update Member m set age = 10")
    .executeUpdate();

  Member findMember = em.find(Member.class, member2.getId());
  System.out.println("findMember.getAge() = " + findMember.getAge());
}
```

```
Hibernate: 
    /* update
        Member m 
    set
        age = 10 */ update Member 
    set
        age=10
Hibernate: 
    select
        m1_0.id,
        m1_0.age,
        m1_0.TEAM_ID,
        m1_0.type,
        m1_0.username 
    from
        Member m1_0 
    where
        m1_0.id=?
findMember.getAge() = 22
```

***결국에는 `.executeUpdate()` 실행 이후에, `em.clear()` 로 영속성 컨텍스트에 아무것도 없게(초기화) 만들어놔야 한다.***

```java
public static void main(String[] args) {
  int count = em.createQuery("update Member m set age = 10")
    .executeUpdate();
  Member beforeClearMember = em.find(Member.class, member2.getId());
  System.out.println("beforeClearMember.getAge() = " + beforeClearMember.getAge());
  System.out.println("member1.getAge() = " + member1.getAge());

  em.clear();

  Member afterClearMember = em.find(Member.class, member2.getId());
  System.out.println("afterClearMember.getAge() = " + afterClearMember.getAge());
  System.out.println("member1.getAge() = " + member1.getAge());
}
```

```
Hibernate: 
    /* update
        Member m 
    set
        age = 10 */ update Member 
    set
        age=10
beforeClearMember.getAge() = 22
member1.getAge() = 22
Hibernate: 
    select
        m1_0.id,
        m1_0.age,
        m1_0.TEAM_ID,
        m1_0.type,
        m1_0.username 
    from
        Member m1_0 
    where
        m1_0.id=?
afterClearMember.getAge() = 10
member1.getAge() = 22
```
