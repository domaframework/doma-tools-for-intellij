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
  public static  List<Project> projects;

  public Integer managerId;

  // accessible instance methods
  public Project getFirstProject() {
    return projects.get(0);
  }

  // Inaccessible instance methods
  private String getEmployeeRank() {
    return rank;
  }

  public Integer employeeParam(Integer p1, Integer p2) {
    return p1 + p2;
  }
}