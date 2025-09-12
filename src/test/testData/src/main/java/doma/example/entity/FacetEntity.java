package doma.example.entity;

import org.seasar.doma.Entity;

/**
 * Entity class at the lowest layer of the inheritance hierarchy
 * {@code FoundationEntity}->{@code LayerNonEntity}->{@code FacetNonEntity}
 */
@Entity
public class FacetEntity extends LayerNonEntity {

	public Integer firstId;
	public String firstName;
	public String lastName;
}
