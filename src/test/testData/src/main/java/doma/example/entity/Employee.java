package doma.example.entity;

import java.util.List;
import java.time.LocalDate;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import doma.example.entity.*;

@Entity
public class Employee extends User {

    @Id
    public Integer employeeId;
    public String employeeName;
    private String department;
    private String rank;
    public static List<Project> projects;

    public Integer managerId;

    // accessible instance methods
    public Project getProject() {
        return projects.get(0);
    }

    public Project getProject(int index) {
        return projects.get(index);
    }

    // Inaccessible instance methods
    private String getEmployeeRank() {
        return rank;
    }

    public Integer employeeParam(String p1, Integer p2) {
        if (p1.isBlank()) {
            return 0;
        }
        return p1.length() + p2;
    }

    public Float employeeParam(int p1, Float p2) {
        return p1 + p2;
    }

    public Project getSubEmployee(Project project){
        return project;
    }

    enum Rank {
        MANAGER,
        STAFF,
        INTERN
    }
}