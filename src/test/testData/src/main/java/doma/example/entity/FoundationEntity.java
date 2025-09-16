package doma.example.entity;

import org.seasar.doma.Entity;
import org.seasar.doma.Id;

/**
 * Entity class at the top of the inheritance hierarchy
 * {@code FoundationEntity}
 */
@Entity
public class FoundationEntity {

	@Id
	Integer id;

	String name;

}
