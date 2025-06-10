package doma.example.dao.inspection;

import doma.example.entity.*;
import org.seasar.doma.*;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.SelectOptions;

import java.util.List;
import java.util.function.BiFunction;

@Dao
interface TestDataCheckDao {

  @Select
  EmployeeSummary literalDirective(String literalName, Integer literalId, Integer literalAge, Boolean literalTrue, Boolean literalFalse, Employee literalNull);

  @Insert(sqlFile=true)
  int bindVariableDirective(Employee employee);

  @Select
  List<Employee> conditionAndLoopDirective(List<Project> projects,LocalDate referenceDate);

  @Select
  Employee commentBlock(Integer id, List<Integer> subIds);

  @Update(sqlFile=true)
  int populateDirective(Employee employee);

  @Insert(sqlFile=true)
  int invalidTestData(Employee employee);

}

