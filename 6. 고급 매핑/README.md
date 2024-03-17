# 6. 고급 매핑

## Index
- [상속 관계 매핑](#상속-관계-매핑)
  - [주요 어노테이션](#주요-어노테이션)
  - [조인 전략](#조인-전략)
  - [단일 테이블 전략](#단일-테이블-전략)
  - [구현 클래스마다 테이블 전략](#구현-클래스마다-테이블-전략)
- [@MappedSuperclass](#mappedsuperclass)
  - [@MappedSuperclass 예시](#mappedsuperclass-예시)

## 상속 관계 매핑

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
