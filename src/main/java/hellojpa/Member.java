package hellojpa;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "MEMBER_ID")
  private Long id;

  @Column(name = "USERNAME")
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "TEAM_ID")
  private Team team;

  @Embedded
  private Address homeAddress;

  @ElementCollection
  @CollectionTable(
    name = "FAVORITE_FOOD",
    joinColumns = @JoinColumn(name = "MEMBER_ID")
  )
  @Column(name = "FOOD_NAME")
  private Set<String> favoriteFoods = new HashSet<>();
//  @OrderColumn(name = "address_history_order")
//  @ElementCollection
//  @CollectionTable(
//    name = "ADDRESS",
//    joinColumns = @JoinColumn(name = "MEMBER_ID")
//  )
//  private List<Address> addressHistory = new ArrayList<>();

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "MEMBER_ID")
  private List<AddressEntity> addressHistory = new ArrayList<>();

  public Member() {
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

  public Team getTeam() {
    return team;
  }

  public void setTeam(Team team) {
    this.team = team;
  }

  public Address getHomeAddress() {
    return homeAddress;
  }

  public void setHomeAddress(Address homeAddress) {
    this.homeAddress = homeAddress;
  }

  public Set<String> getFavoriteFoods() {
    return favoriteFoods;
  }

  public void setFavoriteFoods(Set<String> favoriteFoods) {
    this.favoriteFoods = favoriteFoods;
  }


}
