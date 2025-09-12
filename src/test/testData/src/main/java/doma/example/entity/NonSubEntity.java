package doma.example.entity;

import org.seasar.doma.Table;

/** Class that inherits parent's @Entity without having @Entity itself */
@Table(name = "sub_entity_2")
public class NonSubEntity extends ParentEntity {

  Integer amount;

  String subName;
}
