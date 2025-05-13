package doma.example.dao;

import doma.example.entity.*:
import org.seasar.doma.*;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.SelectOptions;

import java.util.List;
import java.util.function.BiFunction;
import java.util.Optional;

@Dao
interface SqlCompleteTestDao {

  @Select
  Employee completeDaoArgument(Employee employee, String name);

  @Select
  Employee completeInstancePropertyFromDaoArgumentClass(Employee employee, String name);

  @Insert(sqlFile = true)
  int completeJavaPackageClass(Employee employee);

  @Update(sqlFile = true)
  int completeDirective(Employee employee);

  @BatchInsert(sqlFile = true)
  int completeBatchInsert(List<Employee> employees);

  @Select
  Project completeStaticPropertyFromStaticPropertyCall(ProjectDetail detail);

  @Select
  Project completePropertyAfterStaticPropertyCall();

  @Select
  Project completeBuiltinFunction(ProjectDetail detail);

  @Update(sqlFile = true)
  int completeDirectiveInsideIf(Employee employee,Project project);

  @Update(sqlFile = true)
  int completeDirectiveInsideElseIf(Employee employee,Project project);

  @Update(sqlFile = true)
  int completeDirectiveInsideFor(Employee employee,Project project);

  @Update(sqlFile = true)
  int completeDirectiveFieldInsideIf(Employee employee,Project project);

  @Update(sqlFile = true)
  int completeDirectiveFieldInsideElseIf(Employee employee,Project project);

  @Update(sqlFile = true)
  int completeDirectiveFieldInsideFor(Employee employee,Project project);

  @Select
  EmployeeSummary completeComparisonOperator(EmployeeSummary summary);

  @Insert(sqlFile = true)
  int completeConcatenationOperator(Employee employee,Integer point);

  @Select
  Employee completeParameterFirst(Employee employee);

  @Select
  Employee completeParameterFirstProperty(Employee employee);

  @Select
  Employee completeParameterSecond(Employee employee);

  @Select
  Employee completeParameterSecondProperty(Employee employee);

  @Select
  Employee completeCallStaticPropertyClassPackage();

  @Select
  Employee completeCallStaticPropertyClass();

  @Select
  Principal completeForItemHasNext(Principal principal);

  @Select
  Principal completeForItemIndex(Principal principal);

  @Select
  Project completeOptionalDaoParam(Optional<Project> project);

  @Select
  Project completeOptionalStaticProperty();

  @Select
  Project completeOptionalByForItem(List<Project> projects);

  @BatchDelete(sqlFile = true)
  int completeOptionalBatchAnnotation(Optional<List<Optional<Project>>> projects);

  @Select
  Employee completeForDirectiveItem(List<Project> projects);

}