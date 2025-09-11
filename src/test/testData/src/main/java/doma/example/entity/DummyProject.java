package doma.example.entity;

import doma.example.domain.DummyProjectNumber;
import org.seasar.doma.Entity;

@Entity
public class DummyProject extends Project {
    Integer id ;
    DummyProjectNumber number;
    String dummyName;

    @Override
    public String getEmployeeName(int index) {
        return employees.get(index).getUserNameFormat();
    }
}
