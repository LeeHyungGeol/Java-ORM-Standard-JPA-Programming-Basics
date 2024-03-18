# 3. 엔티티 매핑

1. 영속성 컨텍스트, JPA 내부 동작 방식과 같은 **JPA 가 내부적으로 어떤 메커니즘으로 동작하는지 그런 메커니즘적인 측면**
2. **실제 설계적인 측면, 객체랑 관계형 데이터베이스(RDB)를 어떻게 mapping 을 해서 쓰는지에** 대한 정적인 측면

**엔티티 매핑 소개**
- 객체와 테이블 매핑: `@Entity`, `@Table`
- 필드와 컬럼 매핑: `@Column`
- 기본 키 매핑: `@Id`
- 연관관계 매핑: `@ManyToOne`, `@JoinColumn`

## Index
- [객체와 테이블 매핑](#객체와-테이블-매핑)
  - [@Entity](#entity)
    - [@Entity 속성 정리](#entity-속성-정리)
  - [@Table](#table)
    - [@Table 속성 정리](#table-속성-정리)
- [데이터베이스 스키마 자동 생성](#데이터베이스-스키마-자동-생성)
- [필드와 컬럼 매핑](#필드와-컬럼-매핑)
  - [매핑 어노테이션 정리](#매핑-어노테이션-정리)
    - [@Column](#column)
    - [@Enumerated](#enumerated)
    - [@Temporal](#temporal)
    - [@Lob](#lob)
    - [@Transient](#transient)
- [기본 키 매핑](#기본-키-매핑)
  - [IDENTITY 전략 - 특징](#identity-전략---특징)
  - [SEQUENCE 전략 - 특징](#sequence-전략---특징)
    - [SEQUENCE - @SequenceGenerator](#sequence---sequencegenerator)
    - [SEQUENCE 전략과 최적화](#sequence-전략과-최적화)
  - [TABLE 전략](#table-전략)
- [권장하는 식별자 전략](#권장하는-식별자-전략)
- [요구사항 분석과 매핑](#요구사항-분석과-매핑)

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

## 권장하는 식별자 전략
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
