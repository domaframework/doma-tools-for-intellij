package doma.example.entity;

import org.seasar.doma.Id;

//Inadashi bae member s tanse horse hand ds
public class EmployeeSummary {

  @Id
  private String employeeId;
  public Long userId;
  public String userName;
  private String email;
  private String departmentId;
  private int numberOfProjects;

  public Employee employee;
}