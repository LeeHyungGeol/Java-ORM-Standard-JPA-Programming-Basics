# 8. 값 타입

> *임베디드 타입* 과 *값타입 컬렉션* 이 2가지가중요!!!

## Index
- [기본값 타입](#기본값-타입)
  - [JPA의 데이터 타입 분류](#jpa의-데이터-타입-분류)
  - [값 타입 분류](#값-타입-분류)
  - [기본값 타입](#기본값-타입)
  - [참고: 자바의 기본 타입은 절대 공유X](#참고-자바의-기본-타입은-절대-공유x)
- [임베디드 타입(복합 값 타입)](#임베디드-타입복합-값-타입)
  - [임베디드 타입](#임베디드-타입)
  - [임베디드 타입에 대한 예시](#임베디드-타입에-대한-예시)
  - [임베디드 타입 사용법](#임베디드-타입-사용법)
  - [임베디드 타입의 장점](#임베디드-타입의-장점)
  - [임베디드 타입과 테이블 매핑](#임베디드-타입과-테이블-매핑)
  - [임베디드 타입과 연관관계](#임베디드-타입과-연관관계)
  - [@AttributeOverride: 속성 재정의](#attributeoverride-속성-재정의)
  - [임베디드 타입과 null](#임베디드-타입과-null)
- [값 타입과 불변 객체](#값-타입과-불변-객체)
  - [값 타입 공유 참조](#값-타입-공유-참조)
- [값 타입 복사](#값-타입-복사)
  - [객체 타입의 한계](#객체-타입의-한계)
  - [불변 객체](#불변-객체)
- [값 타입의 비교](#값-타입의-비교)
- [값 타입 컬렉션](#값-타입-컬렉션)
  - [값 타입 컬렉션 사용](#값-타입-컬렉션-사용)
    - [값 타입 저장 예제](#값-타입-저장-예제)
    - [값 타입 조회 예제](#값-타입-조회-예제)
    - [값 타입 수정 예제](#값-타입-수정-예제)
  - [값 타입 컬렉션의 제약사항](#값-타입-컬렉션의-제약사항)
  - [값 타입 컬렉션 대안](#값-타입-컬렉션-대안)
- [정리](#정리)

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

## 값 타입 복사

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

## 정리

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
