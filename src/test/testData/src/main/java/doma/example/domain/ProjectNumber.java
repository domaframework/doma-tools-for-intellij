package doma.example.domain;

import org.seasar.doma.Domain;

@Domain(valueType = String.class)
public class ProjectNumber {
	private final String number;

	public ProjectNumber(String nmb){
		number = nmb;
	}

	public String getNumber(){
		return  number;
	}
}
