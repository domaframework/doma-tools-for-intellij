package doma.example.entity;

import doma.example.domain.ClientUser;
import doma.example.domain.ClientUserHasNonEmbeddable;
import org.seasar.doma.Column;
import org.seasar.doma.Entity;

/** Entity class with Embeddable property */
@Entity
public class DepartmentHasNonEmmbedable {
  public Integer id;
  public String name;

  @Column(updatable = false)
  public String location;

  public Integer managerCount;

  int subId;

  public ClientUserHasNonEmbeddable embeddableEntity;

}
