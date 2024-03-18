# 5. 다양한 연관관계 매핑

## Index
- [연관관게 매핑 고려사항 3가지](#연관관게-매핑-고려사항-3가지)
  - [다중성](#다중성)
  - [단방향, 양방향](#단방향-양방향)
  - [연관관계의 주인](#연관관계의-주인)
- [다대일 [N:1]](#다대일-n1)
  - [다대일 단방향](#다대일-단방향)
  - [다대일 양방향](#단방향-양방향)
- [일대다 [1:N]](#일대다-1n)
  - [일대다 단방향](#일대다-단방향)
    - [일대다 단방형 예시](#일대다-단방향-예시)
    - [일대다 단방향 정리](#일대다-단방향-정리)
    - [일대다 단방향 매핑의 단점](#일대다-단방향-매핑의-단점)
  - [일대다 양방향](#일대다-양방향)
    - [일대다 양방향 정리](#일대다-양방향-정리)
- [일대일 [1:1]](#일대일-11)
  - [일대일: 주 테이블에 외래 키 단방향](#일대일-주-테이블에-외래-키-단방향)
  - [일대일: 주 테이블에 외래 키 양방향](#일대일-주-테이블에-외래-키-양방향)
  - [일대일: 대상 테이블에 외래 키 단방향](#일대일-대상-테이블에-외래-키-단방향)
  - [일대일: 대상 테이블에 외래 키 양방향](#일대일-대상-테이블에-외래-키-양방향)
  - [일대일 정리](#일대일-정리)
- [다대다 [N:M]](#다대다-nm)
  - [다대다 매핑의 한계](#다대다-매핑의-한계)
  - [다대다 한계 극복](#다대다-한계-극복)
- [@JoinColumn](#joincolumn)
- [@ManyToOne - 주요 속성](#manytoone---주요-속성)
- [@OneToMany - 주요 속성](#onetomany---주요-속성)


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

### 일대다 단방향

#### 일대다 단방향 예시

- 실무에서 절대 권장하지 않는 방식
- 그러나 있을 수 있는 상황이긴 하다.
- 표준 스펙에서도 지원하긴 하다.

<img width="860" alt="스크린샷 2024-02-10 오후 10 39 41" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/725ba586-3c9b-4a99-a6b2-7ca0d11aa9d4">

- DB 설계상 N 쪽에 외래키(FK) 가 들어갈 수 밖에 없다.
- 이 상황에선 `Team.members` 가 연관관계의 주인이 되는 것이다.
- **그래서, `Team.members` 를 insert, update 할 시에 Member 테이블의 `TEAM_ID` 도 변경해줘야 한다.**

#### 일대다 단방향 정리

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

#### 일대다 단방향 매핑의 단점
- **엔티티가 관리하는 외래 키가 다른 테이블에 있음**
- 연관관계 관리를 위해 추가로 UPDATE SQL 실행
- 일대다 단방향 매핑보다는 **다대일 양방향 매핑**을 사용하자
  - 다대일 단방향이 조금 객체적으로 손해(?)를 볼 수 있더라도, 이 방법이 더 깔끔하다.
    - 손해를 본다는 얘기는 Member 에서 Team 에 대한 참조가 필요없을 수 있는데도, Team 참조를 갖고 있어야 한다.
- 결론적으로, 다대일 단방향, 양방향을 권장하고 그것만 사용한다면, 일대다 단방향은 몰라도 된다!!!!!!

### 일대다 양방향

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

#### 일대다 양방향 정리

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

## 다대다 [N:M]

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

## @JoinColumn
- **외래 키(FK)를 매핑할 때 사용**

<img width="711" alt="스크린샷 2024-02-22 오전 12 59 09" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/25a48861-4810-417e-a8f3-679a37a44e7a">

## @ManyToOne - 주요 속성
- 다대일 관계 매핑

<img width="705" alt="스크린샷 2024-02-22 오전 12 59 26" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/21e63396-b43b-4884-b8dd-fcf1f0122d34">

## @OneToMany - 주요 속성
- 일대다 관계 매핑

<img width="703" alt="스크린샷 2024-02-22 오전 12 59 57" src="https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/d3e891d9-5e11-454f-9e3a-1e0529cc4fce">
