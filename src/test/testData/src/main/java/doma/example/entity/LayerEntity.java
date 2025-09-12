package doma.example.entity;

import org.seasar.doma.Entity;
import org.seasar.doma.Table;

/**
 * Entity class in the middle of the inheritance hierarchy
 * {@code FoundationEntity}->{@code LayerEntity}
 */
@Entity
@Table(name = "sub_entity_1")
public class LayerEntity extends FoundationEntity {
  Integer amount;

  String subName;
}
