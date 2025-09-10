package doma.example.domain;

import org.seasar.doma.Domain;

@Domain(valueType = String.class)
public class DummyProjectNumber extends ProjectNumber {
	private final String dummyNumber;

	public DummyProjectNumber(String nmb){
		super(nmb);
		dummyNumber = nmb;
	}

	public String getDummyNumber(){
		return  dummyNumber;
	}
}
