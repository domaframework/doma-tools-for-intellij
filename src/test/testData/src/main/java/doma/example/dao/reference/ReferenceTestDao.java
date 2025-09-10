package doma.example.dao.reference;

import java.util.List;
import doma.example.entity.*;
import java.util.Map;
import java.util.Set;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Dao
public interface ReferenceTestDao {

  @Select
  Map<String, Object> referenceDaoParameter(Long reportId, String tableName, Set<String> columns);

  @Select
  Project referenceEntityProperty(List<Project> projects, ProjectDetail detail);

  @Select
  ProjectDetail referenceStaticField(ProjectDetail detail);

  @Select
  List<Employee> referenceListFieldMethod(List<Employee> employeesList);

  @Select
  List<Employee> referenceForItem(List<List<Employee>> employeesList);

  @Select
  Project referenceCustomFunction(ProjectDetail detail);

  @Select
  Employee referenceMethodParameter(float floatValue, int intValue
          , Employee employee
          , Project project, DummyProject subProject
          , LocalDate localDate, LocalDateTime localDateTime
          , CharSequence charSeq, String str
          , List<ColumnEntity> columns);

    @Select
    Employee documentOverloadInstanceMethod1(Employee employee);

    @Select
    Employee documentOverloadInstanceMethod2(Employee employee, float floatVal);

    @Select
    Employee documentOverloadStaticMethod1(Employee employee);

    @Select
    Employee documentOverloadStaticMethod2(Employee employee);

    @Select
    Employee documentOverloadCustomFunction1(Employee employee);

    @Select
    Employee documentOverloadCustomFunction2(Project project);

    @Select
    Employee documentOverloadBuiltInFunction1(Date date);

    @Select
    Employee documentOverloadBuiltInFunction2(LocalDateTime localDateTime);


}