package hellojpa;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class CascadeParent {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  private String name;

  @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
  private List<CascadeChild> childList = new ArrayList<>();

  public void addChild(CascadeChild child) {
    childList.add(child);
    child.setParent(this);
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

  public List<CascadeChild> getChildList() {
    return childList;
  }
}
