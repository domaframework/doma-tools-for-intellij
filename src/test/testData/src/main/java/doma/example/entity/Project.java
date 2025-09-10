package doma.example.entity;

import doma.example.domain.DummyProjectNumber;
import doma.example.domain.ProjectNumber;
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
    public static Integer projectNumber;
    private static String projectCategory;

    public static List<Employee> employees = new ArrayList<>();

    public static Integer cost;

    public static Employee getFirstEmployee() {
        return employees.get(0);
    }

    // Accessible Static methods
    public static String getTermNumber() {
        return projectNumber.toString() + "_term";
    }

    public static Integer calculateCost(Integer base, Integer multiplier) {
        return base * multiplier;
    }

    public static String formatName(String prefix, String suffix) {
        return prefix + "_" + suffix;
    }

    // Static methods that are not accessible
    private static String getCategoryName() {
        return projectCategory + "_term";
    }

    public static Optional<Employee> getEmployee(Float val, Integer index){
        return employees.stream().filter( e -> e.getProject(index).rank >= val.intValue()).findFirst();
    }

    public static Employee getEmployee(int index) {
        return employees.get(index);
    }

    public static Employee getEmployee(Employee employee) {
        return employee;
    }

    public static Project getEmployeeByProject(int index, Project project){
        if(index >= 0){
            return employees.get(index).getSubEmployee(project);
        }
        return project;
    }

    public static String getProjectNumber(int index, ProjectNumber number) {
        if (index >= 100) {
            return number.getNumber();
        }
        if (number instanceof DummyProjectNumber)
            return ((DummyProjectNumber) number).getDummyNumber();
        else
            return "";
    }
}