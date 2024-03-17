# 9. 객체지향 쿼리 언어(JPQL) - 1. 기본문법

## Index
- [객체지향 쿼리 언어 소개](#객체지향-쿼리-언어-소개)
  - [JPA는 다양한 쿼리 방법을 지원](#jpa는-다양한-쿼리-방법을-지원)
  - [JPQL 소개](#jpql-소개)
    - [JPQL: 엔티티 객체를 대상으로 하는 객체 지향 SQL](#jpql-엔티티-객체를-대상으로-하는-객체-지향-sql)
    - [JPQL 과 SQL](#jpql-과-sql)
  - [Criteria 소개](#criteria-소개)
  - [QueryDSL 소개](#querydsl-소개)
  - [네이티브 SQL 소개](#네이티브-sql-소개)
  - [JDBC 직접 사용, SpringJdbcTemplate 등](#jdbc-직접-사용-springjdbctemplate-등)
- [JPQL(Java Persistence Query Language)](#jpqljava-persistence-query-language)
  - [JPQL - 기본 문법과 기능](#jpql---기본-문법과-기능)
  - [JPQL 소개 - JPQL은 SQL을 추상화해서 특정 데이터베이스 SQL에 의존하지 않는다.](#jpql-소개---jpql은-sql을-추상화해서-특정-데이터베이스-sql에-의존하지-않는다)
- [JPQL 문법](#jpql-문법)
  - [집합과 정렬](#집합과-정렬)
  - [TypedQuery, Query](#typedquery-query)
  - [결과 조회 API](#결과-조회-api)
  - [파라미터 바인딩 - 이름 기준, 위치 기준](#파라미터-바인딩---이름-기준-위치-기준)
- [프로젝션](#프로젝션)
  - [SELECT m FROM Member m -> 엔티티 프로젝션(entity projection)](#select-m-from-member-m---엔티티-프로젝션entity-projection)
  - [SELECT **m.team** FROM Member m -> 엔티티 프로젝션(entity projection)](#select-mteam-from-member-m---엔티티-프로젝션entity-projection)
  - [SELECT **m.address** FROM Member m -> 임베디드 타입 프로젝션(embedded type projection)](#select-maddress-from-member-m---임베디드-타입-프로젝션embedded-type-projection)
  - [SELECT **m.username, m.age** FROM Member m -> 스칼라 타입 프로젝션(scalar type projection)](#select-musername-mage-from-member-m---스칼라-타입-프로젝션scalar-type-projection)
  - [프로젝션 - 여러 값 조회](#프로젝션---여러-값-조회)
    - [1. Query 타입으로 조회](#1-query-타입으로-조회)
    - [2. Object[] 타입으로 조회](#2-object-타입으로-조회)
    - [3. new 명령어로 조회](#3-new-명령어로-조회)
- [페이징 API](#페이징-api)
  - [페이징 API 예시](#페이징-api-예시)
  - [페이징 API - MySQL 방언](#페이징-api---mysql-방언)
  - [페이징 API - Oracle 방언](#페이징-api---oracle-방언)
- [조인](#조인)
  - [내부 조인 INNER JOIN](#내부-조인-inner-join)
  - [외부 조인 LEFT [OUTER] JOIN](#외부-조인-left-outer-join)
  - [세타 조인: 아무런 연관관계가 없는 필드로 조인하는 방법](#세타-조인-아무런-연관관계가-없는-필드로-조인하는-방법)
- [조인 - ON 절](#조인---on-절)
  - [1. 조인 대상 필터링](#1-조인-대상-필터링)
  - [2. 연관관계 없는 엔티티 외부 조인](#2-연관관계-없는-엔티티-외부-조인)
- [서브 쿼리](#서브-쿼리)
  - [서브 쿼리 지원 함수](#서브-쿼리-지원-함수)
  - [서브 쿼리 - 예제](#서브-쿼리---예제)
  - [JPA 서브 쿼리 한계: FROM 절의 서브 쿼리는 현재 JPQL에서 불가능 (조인으로 풀 수 있으면 풀어서 해결)](#jpa-서브-쿼리-한계-from-절의-서브-쿼리는-현재-jpql에서-불가능-조인으로-풀-수-있으면-풀어서-해결)
  - [하이버네이트 6 변경 사항: 하이버네이트 6 부터는 FROM 절의 서브쿼리를 지원 합니다.](#하이버네이트-6-변경-사항-하이버네이트-6-부터는-from-절의-서브쿼리를-지원-합니다)
- [JPQL 타입 표현](#jpql-타입-표현)
  - [JPQL 기타](#jpql-기타)
- [조건식 - CASE 식](#조건식---case-식)
  - [기본 CASE 식](#기본-case-식)
  - [단순 CASE 식: case 값을 명확하게 명시하는 case 문](#단순-case-식-case-값을-명확하게-명시하는-case-문)
  - [조건식 - COALESCE,NULLIF](#조건식---coalescenullif)
    - [COALESCE: 사용자 이름이 ‘관리자’면 null을 반환하고 나머지는 본인의 이름을 반환](#coalesce-사용자-이름이-관리자면-null을-반환하고-나머지는-본인의-이름을-반환)
    - [NULLIF: 사용자 이름이 없으면 이름 없는 회원을 반환](#nullif-사용자-이름이-없으면-이름-없는-회원을-반환)
- [JPQL 기본 함수](#jpql-기본-함수)
    - [SIZE: 컬렉션의 크기를 반환](#size-컬렉션의-크기를-반환)
  - [사용자 정의 함수 호출](#사용자-정의-함수-호출)
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

## 객체지향 쿼리 언어 소개

### JPA는 다양한 쿼리 방법을 지원

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


#### JPQL: 엔티티 객체를 대상으로 하는 객체 지향 SQL

- SQL 과 굉장히 유사한 문법이 제공
- JPA를 사용하면 **엔티티 객체를 중심으로 개발**
- 문제는 검색 쿼리
- 검색을 할 때도 **테이블이 아닌 엔티티 객체를 대상으로 검색**
- 모든 DB 데이터를 객체로 변환해서 검색하는 것은 불가능
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL이 필요
- JPA는 **SQL을 추상화**한 JPQL이라는 ***객체 지향 쿼리 언어 제공***
- SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원

#### JPQL 과 SQL

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

### Criteria 소개

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


### QueryDSL 소개

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


### 네이티브 SQL 소개

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

### JDBC 직접 사용, SpringJdbcTemplate 등

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

## JPQL 문법

![객체지향 쿼리 언어2](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/1d50005e-70cf-4968-97e3-ff9b1230bd1d)
- update, delete 문은 **bulk 연산**에 사용된다.
  - 한방에 여러개의 data 를 변경할 때 사용한다.
    - jpa 는 원래 값이 변경되면 트랜잭션 커밋 시점에 update 쿼리를 날린다.
    - 다만, 이것은 한건 한건씩 변경되는 것이고, 여러개를 한번에 변경할 경우에는 bulk 연산을 사용한다.
    - bulk 연산은 jpa 에서 따로 관리한다.

- select m from **Member** as m where **m.age** > 18
- 엔티티와 속성은 대소문자 구분 O (Member, age)
- JPQL 키워드는 대소문자 구분 X (SELECT, FROM, where)
- 엔티티 이름 사용, 테이블 이름이 아님(Member)
- **별칭은 필수(m)** (as는 생략가능)


### 집합과 정렬

```sql
select
    COUNT (m), //회원수
    SUM (m.age), //나이 합
    AVG (m.age), //평균 나이
    MAX (m.age), //최대 나이
    MIN (m.age) //최소 나이
from Member m
```

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


### 파라미터 바인딩 - 이름 기준, 위치 기준

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


### SELECT m FROM Member m -> 엔티티 프로젝션(entity projection)

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

### SELECT **m.team** FROM Member m -> 엔티티 프로젝션(entity projection)

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

### SELECT **m.address** FROM Member m -> 임베디드 타입 프로젝션(embedded type projection)

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

### SELECT **m.username, m.age** FROM Member m -> 스칼라 타입 프로젝션(scalar type projection)

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
  - 설정한 Database 방언(Dialect)에 맞게 sql 을 알맞게 날려준다.
```xml
<property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
<property name="hibernate.dialect" value="org.hibernate.dialect.OracleDialect"/>
<property name="hibernate.dialect" value="org.hibernate.dialect.MySQLDialect"/>
```

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


## 조인

***정석적인 것은 역시 양방향으로 연관관계를 set 해주는 것!! **연관관계 편의 메소드**를 만들어주는 것이 좋다!!!***

### 내부 조인 INNER JOIN

- 내부 조인: SELECT m FROM Member m [INNER] JOIN m.team t
  - 내부조인은 member 는 있고 team 은 없으면 이 데이터가 아예 안나온다.
    - join 도 객체스럽게 표현한다.
      - JOIN m.team t

```java
public static void main(String[] args) {
  Team team = new Team();
  team.setName("team1");
  em.persist(team);

  Member member = new Member();
  member.setUsername("member1");
  member.setAge(22);
  member.changeTeam(team);
  em.persist(member);

  em.flush();
  em.clear();

  String innerJoinJpql = "select m from Member m inner join m.team t";
  List<Member> list = em.createQuery(innerJoinJpql, Member.class)
    .getResultList();
}
```

```
Hibernate: 
    /* select
        m 
    from
        Member m 
    inner join
        m.team t */ select
            m1_0.MEMBER_ID,
            m1_0.age,
            m1_0.TEAM_ID,
            m1_0.username 
        from
            Member m1_0 
        join
            Team t1_0 
                on t1_0.TEAM_ID=m1_0.TEAM_ID
```

### 외부 조인 LEFT [OUTER] JOIN

- 외부 조인: SELECT m FROM Member m LEFT [OUTER] JOIN m.team t
  - 외부조인은 member 는 있고 team 은 없으면 team 의 데이터는 null 로 표현하고, 데이터가 나온다.

```java
public static void main(String[] args) {
  Team team = new Team();
  team.setName("team1");
  em.persist(team);

  Member member = new Member();
  member.setUsername("member1");
  member.setAge(22);
  member.changeTeam(team);
  em.persist(member);

  em.flush();
  em.clear();

  String leftOuterJoinJpql = "select m from Member m left outer join Team t on m.id = t.id";
  List<Member> list = em.createQuery(leftOuterJoinJpql, Member.class)
    .getResultList();

}
```

```
Hibernate: 
    /* select
        m 
    from
        Member m 
    left outer join
        Team t 
            on m.id = t.id */ select
                m1_0.MEMBER_ID,
                m1_0.age,
                m1_0.TEAM_ID,
                m1_0.username 
        from
            Member m1_0 
        left join
            Team t1_0 
                on m1_0.MEMBER_ID=t1_0.TEAM_ID
```

### 세타 조인: 아무런 연관관계가 없는 필드로 조인하는 방법

- 세타 조인: select count(m) from Member m, Team t where m.username = t.name
- 정말 연관관계가 없을 때, 막 조인이라고도 한다.
  - member, team 을 둘 다 from 에 넣는다. 그렇게 하면 카르테시안 곱을 통해 데이터가 다 곱하기로 불러진다.
  - where 절의 조건문에 맞는 놈들을 불러온다.

```java
public static void main(String[] args) {
  Team team = new Team();
  team.setName("memberTeam1");
  em.persist(team);

  Member member = new Member();
  member.setUsername("memberTeam1");
  member.setAge(22);
  member.changeTeam(team);
  em.persist(member);

  em.flush();
  em.clear();

  String thetaJoin = "select m from Member m, Team t where m.username = t.name";
  List<Member> list = em.createQuery(thetaJoin, Member.class)
    .getResultList();

  System.out.println("list.size() = " + list.size());

}
```

```
Hibernate: 
    /* select
        m 
    from
        Member m,
        Team t 
    where
        m.username = t.name */ select
            m1_0.MEMBER_ID,
            m1_0.age,
            m1_0.TEAM_ID,
            m1_0.username 
        from
            Member m1_0,
            Team t1_0 
        where
            m1_0.username=t1_0.name
list.size() = 1
```

## 조인 - ON 절

- ON절을 활용한 조인(JPA 2.1부터 지원)
- 1. 조인 대상 필터링
- 2. 연관관계 없는 엔티티 외부 조인(하이버네이트 5.1부터)


### 1. 조인 대상 필터링

```java
public static void main(String[] args) {
  Team team = new Team();
  team.setName("memberTeam1");
  em.persist(team);

  Member member = new Member();
  member.setUsername("memberTeam1");
  member.setAge(22);
  member.changeTeam(team);
  em.persist(member);

  em.flush();
  em.clear();

  String query = "select m,t from Member m left join m.team t on t.name = 'memberTeam1'";
  em.createQuery(query)
    .getResultList();
}
```

```
Hibernate: 
    /* select
        m,
        t 
    from
        Member m 
    left join
        m.team t 
            on t.name = 'memberTeam1' */ select
                m1_0.MEMBER_ID,
                m1_0.age,
                m1_0.TEAM_ID,
                m1_0.username,
                t1_0.TEAM_ID,
                t1_0.name 
        from
            Member m1_0 
        left join
            Team t1_0 
                on t1_0.TEAM_ID=m1_0.TEAM_ID 
                and t1_0.name='memberTeam1'
```

- 예) 회원과 팀을 조인하면서, 팀 이름이 A인 팀만 조인
- JPQL: SELECT m, t FROM Member m LEFT JOIN m.team t on t.name = 'A'
- SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.TEAM_ID=t.id and t.name='A'

### 2. 연관관계 없는 엔티티 외부 조인

```java
public static void main(String[] args) {
  Team team = new Team();
  team.setName("memberTeam1");
  em.persist(team);

  Member member = new Member();
  member.setUsername("memberTeam1");
  member.setAge(22);
  member.changeTeam(team);
  em.persist(member);

  em.flush();
  em.clear();

  String query = "select m,t from Member m left join Team t on m.username = t.name";
  em.createQuery(query)
    .getResultList();

}
```

```
Hibernate: 
    /* select
        m,
        t 
    from
        Member m 
    left join
        Team t 
            on m.username = t.name */ select
                m1_0.MEMBER_ID,
                m1_0.age,
                m1_0.TEAM_ID,
                m1_0.username,
                t1_0.TEAM_ID,
                t1_0.name 
        from
            Member m1_0 
        left join
            Team t1_0 
                on m1_0.username=t1_0.name
```

- 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
- JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
- SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name

> 세타 조인: 아무런 연관관계가 없는 필드로 조인하는 방법

> 세타조인은 동등조인이면서 동시에 sql에서 join구문 없이 사용하는 것

## 서브 쿼리

- 나이가 평균보다 많은 회원
  - 메인 쿼리의 Member m 과 서브 쿼리의 Member m2 가 서로 다른 Member 이다.
  - 이렇게 하면 성능이 괜찮다.

```java
select m from Member m where m.age > (select avg(m2.age) from Member m2)
```
- 한 건이라도 주문한 고객
  - 위의 예제와 달리 메인 쿼리의 Order o 와 서브 쿼리의 Order o 가 같은 Order 이다.
  - 이렇게 하면 성능이 잘 안나온다.

```java
select m from Member m where (select count(o) from Order o where m = o.member) > 0
```

### 서브 쿼리 지원 함수

- [NOT] EXISTS (subquery): 서브쿼리에 결과가 존재하면 참
  - {ALL | ANY | SOME} (subquery)
  - ALL 모두 만족하면 참
  - ANY, SOME: 같은 의미, 조건을 하나라도 만족하면 참
- [NOT] IN (subquery): 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참


### 서브 쿼리 - 예제

- 팀A 소속인 회원
  - `select m from Member m where exists (select t from m.team t where t.name = ‘팀A')`
- 전체 상품 각각의 재고보다 주문량이 많은 주문들
  - `select o from Order o where o.orderAmount > ALL (select p.stockAmount from Product p)`
- 어떤 팀이든 팀에 소속된 회원
  - `select m from Member m where m.team = ANY (select t from Team t)`


### JPA 서브 쿼리 한계: FROM 절의 서브 쿼리는 현재 JPQL에서 불가능 (조인으로 풀 수 있으면 풀어서 해결)

- JPA는 WHERE, HAVING 절에서만 서브 쿼리 사용 가능
- SELECT 절도 가능(하이버네이트에서 지원)
- **FROM 절의 서브 쿼리는 현재 JPQL에서 불가능 -> Hibernate 6 부터 가능!!**
  - **조인으로 풀 수 있으면 풀어서 해결**
  - EX) `select mm.age, mm.username from (select m.age as age, m.username as username from Member as m) as mm`


### 하이버네이트 6 변경 사항: 하이버네이트 6 부터는 FROM 절의 서브쿼리를 지원 합니다.

- 하이버네이트 6 부터는 FROM 절의 서브쿼리를 지원 합니다.
- 참고 링크
- https://in.relation.to/2022/06/24/hibernate-orm-61-features/


## JPQL 타입 표현

```java
String query = "select m.username, 'HELLO', true from Member m";
em.createQuery(query)
    .getResultList();
```

```java
String query = "select m.username, 'HELLO', true from Member m where m.type = org.example.jpql.domain.MemberType.USER";
em.createQuery(query)
    .getResultList();
```

```java
em.createQuery("select i from Item i where type(i) = Book", Item.class)
    .getResultList();
```

- 문자: ‘HELLO’, ‘She’’s’
- 숫자: 10L(Long), 10D(Double), 10F(Float)
- Boolean: TRUE, FALSE
- ENUM: jpabook.MemberType.Admin (패키지명 포함)
- 엔티티 타입: TYPE(m) = Member (상속 관계에서 사용)


### JPQL 기타

```java
String query = "select m.username, 'HELLO', true from Member m where m.username is not null";
em.createQuery(query)
    .getResultList();
```

- SQL과 문법이 같은 식
- EXISTS, IN
- AND, OR, NOT
- =, >, >=, <, <=, <>
- BETWEEN, LIKE, IS NULL


## 조건식 - CASE 식

### 기본 CASE 식

```java
public static void main(String[] args) {
  String query = "select "
    + "case when m.age <= 10 then '학생요금'"
    + "     when m.age >= 60 then '경로요금'"
    + "     else '일반요금' "
    + "end "
    + "from Member m";
  em.createQuery(query).getResultList();
}
```

```
Hibernate: 
    /* select
        case 
            when m.age <= 10 
                then '학생요금'     
            when m.age >= 60 
                then '경로요금'     
            else '일반요금' 
        end 
    from
        Member m */ select
            case 
                when m1_0.age<=10 
                    then '학생요금' 
                when m1_0.age>=60 
                    then '경로요금' 
                else '일반요금' 
            end 
        from
            Member m1_0

```

### 단순 CASE 식: case 값을 명확하게 명시하는 case 문

```java
public static void main(String[] args) {
  String query = "select "
    + "case t.name"
    + "     when '팀A' then '인센티브110%'"
    + "     when '팀B' then '인센티브120%'"
    + "     else '인센티브105%'"
    + "end "
    + "from Team t";
  em.createQuery(query).getResultList();
}
```

```
Hibernate: 
    /* select
        case t.name     
            when '팀A' 
                then '인센티브110%'     
            when '팀B' 
                then '인센티브120%'     
            else '인센티브105%'
        end 
    from
        Team t */ select
            case t1_0.name 
                when '팀A' 
                    then '인센티브110%' 
                when '팀B' 
                    then '인센티브120%' 
                else '인센티브105%' 
            end 
        from
            Team t1_0
```

### 조건식 - COALESCE,NULLIF

- **COALESCE**: 하나씩 조회해서 null이 아니면 반환
- **NULLIF**: 두 값이 같으면 null 반환, 다르면 첫번째 값 반환

#### COALESCE: 사용자 이름이 ‘관리자’면 null을 반환하고 나머지는 본인의 이름을 반환

```java
public static void main(String[] args) {
  ...
  Member member = new Member();
  member.setUsername(null);
  ...
  
  em.flush();
  em.clear();

  String query = "select coalesce(m.username, '이름 없음') from Member m";
  List<String> result = em.createQuery(query).getResultList();

  for (String s : result) {
    System.out.println("username = " + s);
  }
}
```

```
Hibernate: 
    /* select
        coalesce(m.username, '이름 없음') 
    from
        Member m */ select
            coalesce(m1_0.username, '이름 없음') 
        from
            Member m1_0
username = 이름 없음
```

#### NULLIF: 사용자 이름이 없으면 이름 없는 회원을 반환

```java
public static void main(String[] args) {
  ...
  member.setUsername("같은 이름 username");
  ...
  
  em.flush();
  em.clear();

  String query = "select nullif(m.username, '같은 이름 username') from Member m";
  List<String> result = em.createQuery(query).getResultList();

  for (String s : result) {
    System.out.println("username = " + s);
  }
}
```

```
Hibernate: 
    /* select
        nullif(m.username, '같은 이름 username') 
    from
        Member m */ select
            nullif(m1_0.username, '같은 이름 username') 
        from
            Member m1_0
username = null
```

## JPQL 기본 함수

- CONCAT
- SUBSTRING
- TRIM
- LOWER, UPPER
- LENGTH
- LOCATE
- ABS, SQRT, MOD
- SIZE, INDEX(JPA 용도) (INDEX 는 웬만하면 안쓰는게 좋다.)

사실 표준 기본 함수는 몇개 안된다. 나머지는 DB(MySQL, MSSQL, Oracle 등등) 방언에 웬만한 함수들이 다 등록되어 있다

#### SIZE: 컬렉션의 크기를 반환

```java
public static void main(String[] args) {
  String query = "select size(t.members) from Team t";
  List<Integer> result = em.createQuery(query).getResultList();
}
```

```
Hibernate: 
    /* select
        size(t.members) 
    from
        Team t */ select
            (select
                count(1) 
            from
                Member m1_0 
            where
                t1_0.id=m1_0.TEAM_ID) 
        from
            Team t1_0
size = 1
```

### 사용자 정의 함수 호출

- 하이버네이트는 사용전 방언에 추가해야 한다.
  - DB 방언에 추가해놔야 한다.
- 사용하는 DB 방언을 상속받고, 사용자 정의 함수를 등록한다.

1. FunctionContributer의 구현체를 만들어 준다.
```java
public class CustomFunctionContributor implements FunctionContributor {

  @Override
  public void contributeFunctions(FunctionContributions functionContributions) {
    functionContributions
      .getFunctionRegistry()
      .register("group_concat", new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
  }
}
```

2. FunctionContributor 의 구현체를 resources/META-INF 에 등록해준다.
  - `org.hibernate.boot.model.FunctionContributor` 파일을 생성
  - 패키지명.생성한구현체파일명
```
dialect.CustomFunctionContributor
```

```java
public static void main(String[] args) {
  Team team = new Team();
  team.setName("memberTeam1");
  em.persist(team);

  Member member = new Member();
  member.setUsername("같은 이름 username");
  member.setAge(22);
  member.changeTeam(team);
  em.persist(member);

  Member member2 = new Member();
  member2.setUsername("같은 이름 username");
  member2.setAge(22);
  member2.changeTeam(team);
  em.persist(member2);

  em.flush();
  em.clear();

  String query = "select function('group_concat', m.username) from Member m";
  List<String> result = em.createQuery(query).getResultList();

  for (String s : result) {
    System.out.println("s = " + s);
  }
}
```

```
Hibernate: 
    /* select
        function('group_concat', m.username) 
    from
        Member m */ select
            group_concat(m1_0.username) 
        from
            Member m1_0
s = 같은 이름 username,같은 이름 username
```

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


## 페치 조인(fetch join)

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

### 페치 조인 사용 코드

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

### 컬렉션 페치 조인 사용 코드

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
  - 1. SQL에 DISTINCT를 추가
  - 2. 애플리케이션에서 엔티티 중복 제거

![객체지향 쿼리 언어7](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/0919b759-5b61-431c-87d4-871e372122d4)

```sql
select distinct t
from Team t join fetch t.members
where t.name = '팀A'
```

- SQL에 DISTINCT를 추가하지만 데이터가 다르므로 SQL 결과에서 중복제거 실패


### 페치 조인과 DISTINCT

![객체지향 쿼리 언어8](https://github.com/LeeHyungGeol/Programmers_CodingTestPractice/assets/56071088/cadc4770-caa0-49a7-9b6c-155ed6bae2b6)

- DISTINCT 가 추가로 애플리케이션에서 중복 제거시도
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

### 일대가 관계 페치 조인 후 페이징 API 사용 방법

1. **방향을 뒤집어서 해결**: 쿼리 바꾸기 일대다는 다대일로 바꿀 수 있다.
  - `select t from Team t join fetch t.members` -> `select m from Members m join fetch m.team`
2. `@BatchSize` 사용
  - BatchSize 의 크기는 1000 이하의 크기로 적당히 크게 준다.
  - `<property name="hibernate.default_batch_fetch_size" value="100"/>` 이렇게 값을 설정할 수도 있다.
  - Team 을 가져올 때, Member 는 lazy loading 상태이다.
  - lazy loading 상태인 놈을 갖고 올 때,
  - `select t from Team t` 에서 나온 team 의 갯수만큼 `where m1_0.TEAM_ID in(?,?)` 의 in 절에 TEAM_ID 값을 넣어준다.
  - 만약에 150개가 있다면, 처음에 in (?,?..) 에 물음표(TEAM_ID 값)를 100개를 날리고, 두번째는 남은 50개를 날린다.
3. DTO 를 만들어서 사용
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

### 페치 조인의 특징과 한계

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


### 엔티티 직접 사용 - 기본 키 값

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
