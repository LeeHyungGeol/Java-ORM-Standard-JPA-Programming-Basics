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
