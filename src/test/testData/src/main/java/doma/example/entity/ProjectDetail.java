package doma.example.entity;

import doma.example.entity.*;
import java.time.LocalDate;
import org.seasar.doma.Entity;
import org.seasar.doma.Id;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ProjectDetail {

  @Id
  private Integer projectDetailId;
  private Integer projectId;
  private static List<Employee> members = new ArrayList<>();

  // Accessible static fields
  public static Integr projectNumber;
  private static String projectName;
  private static String projectCategory;

  private static Employee manager = new Employee();

  public Employee getFirstEmployee() {
    return employees.get(0);
  }

  // Accessible Static methods
  public static String getTermNumber() {
    return projectNumber.toString()+"_term";
  }

  // Static methods that are not accessible
  private static String getCategoryName() {
    return projectCategory+"_term";
  }

  public void addTermNumber(){
    projectNumber++;
  }

  public static String getCustomNumber(String prefix) {
    return prefix + projectName;
  }

}