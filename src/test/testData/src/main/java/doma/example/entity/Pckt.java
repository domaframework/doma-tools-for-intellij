package doma.example.entity;

import org.seasar.doma.Entity;

@Entity(immutable = true)
public class Pckt {
    private final int id;
    private final String name;

    public Pckt(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public String getName() { return name; }
}

