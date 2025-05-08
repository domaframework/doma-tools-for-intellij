package doma.example.entity;

import java.time.LocalDate;
import org.seasar.doma.*;
import org.seasar.doma.Id;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
public class Project {

  @Id
  private Integer projectId;
  private String projectName;
  private static String status;
  private Integer rank;

  public static Optional<List<Optional<Integer>>> optionalIds;
  public static Optional<User> manager;

  // Accessible static fields
  public static Integr projectNumber;
  private static String projectCategory;

  public static Integer cost;

  public static Employee getFirstEmployee() {
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

}