package doma.example.dao;

import doma.example.entity.*:
import org.seasar.doma.*;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.SelectOptions;

import java.util.List;
import java.util.function.BiFunction;

@Dao
interface EmployeeSummaryDao {

  @Select
  EmployeeSummary bindVariableForNonEntityClass(EmployeeSummary employee, User user);

  @Insert(sqlFile=true)
  EmployeeSummary bindVariableForEntityAndNonEntityParentClass(Employee employee);

  @Update(sqlFile=true)
  int accessStaticProperty(Project project,LocalDate referenceDate);

  @Select
  ProjectDetail resolveDaoArgumentOfListType(List<Employee> employees);

  @BatchInsert(sqlFile=true)
  int[] batchAnnotationResolvesClassInList(List<Employee> employees);

  @Insert(sqlFile=true)
  EmployeeSummary bindVariableInFunctionParameters(Employee employee, User user);
}