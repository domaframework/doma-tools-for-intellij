package doma.example.entity;

import org.seasar.doma.Entity;
import org.seasar.doma.Id;

@Entity
public class ParentEntity {

	@Id
	Integer id;

	String name;

}
