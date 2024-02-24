package hellojpa;

import jakarta.persistence.*;

@Entity
public class Child {
  @Id @GeneratedValue
  private Long id;

  @ManyToOne
  @JoinColumns({
    @JoinColumn(name = "PARENT_ID1"), @JoinColumn(name = "PARENT_ID2")
  })
  private Parent parent;
  private String name;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Parent getParent() {
    return parent;
  }

  public void setParent(Parent parent) {
    this.parent = parent;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
