package hellojpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "Member")
@Table(name = "Mbr")
public class Member {
    @Id
    private Long id;
    private String name;

    // JPA 는 기본적으로 내부적으로 reflection API 를 쓰기 때문에 동적으로 객체를 생성해야 한다.
    // 따라서, 기본 생성자가 꼭 필요하다.
    public Member() {
    }

    public Member(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
