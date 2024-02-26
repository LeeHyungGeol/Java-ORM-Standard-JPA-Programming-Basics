package hellojpa;

import jakarta.persistence.*;

@Entity
public class CascadeChild {
  @Id
  @GeneratedValue
  private Long id;

  private String name;

  @ManyToOne
  @JoinColumn(name = "parent_id")
  private CascadeParent parent;

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

  public CascadeParent getParent() {
    return parent;
  }

  public void setParent(CascadeParent parent) {
    this.parent = parent;
  }
}
