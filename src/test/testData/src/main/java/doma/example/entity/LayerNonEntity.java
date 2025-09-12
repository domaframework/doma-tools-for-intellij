package doma.example.entity;

import org.seasar.doma.Table;

/**
 * Non-Entity class in the middle of the inheritance hierarchy
 * {@code FoundationEntity}->{@code LayerNonEntity}
 * Cannot be used with include/exclude options
 * Cannot be specified as a parameter for certain annotation type DAO methods
 */
@Table(name = "sub_entity_2")
public class LayerNonEntity extends FoundationEntity {

	Integer amount;

	String subName;
}
