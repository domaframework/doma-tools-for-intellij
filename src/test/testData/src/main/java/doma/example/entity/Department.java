package doma.example.entity;

import org.seasar.doma.*;
import doma.example.domain.*;

@Entity
public class Department {
    public Integer id;
    public String name;
    @Column(updatable = false)
    public String location;
    public Integer managerCount;

    int subId;

    @Embedded
    public ClientUser embeddableEntity;
    @Embedded
    public ClientUser embeddableEntity2;
}