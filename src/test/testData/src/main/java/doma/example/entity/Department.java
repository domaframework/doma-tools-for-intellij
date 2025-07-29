package doma.example.entity;

import org.seasar.doma.Entity;

@Entity
public class Department {
    public Integer id;
    public String name;
    public String location;
    public Integer managerCount;
}