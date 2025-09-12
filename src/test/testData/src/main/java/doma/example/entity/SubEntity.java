package doma.example.entity;

import org.seasar.doma.Entity;
import org.seasar.doma.Table;

/** Class with @Entity on both itself and parent */
@Entity
@Table(name = "sub_entity_1")
public class SubEntity extends ParentEntity {
  Integer amount;

  String subName;
}
