package doma.example.dao.inspection;

import doma.example.entity.*;
import org.seasar.doma.*;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.SelectOptions;
import java.util.*;

import java.util.List;
import java.util.function.BiFunction;

@Dao
interface ParamDefinedTestDao {

  @Select
  EmployeeSummary bindVariableForNonEntityClass(EmployeeSummary employee, User user);

  @Insert(sqlFile=true)
  EmployeeSummary bindVariableForEntityAndNonEntityParentClass(Employee employee);

  @Update(sqlFile=true)
  int accessStaticProperty(Project project,LocalDate referenceDate);

  @Select
  int callStaticPropertyPackageName();

  @Select
  ProjectDetail resolveDaoArgumentOfListType(List<Employee> employees);

  @BatchInsert(sqlFile=true)
  int[] batchAnnotationResolvesClassInList(List<Employee> employees);

  @Insert(sqlFile=true)
  EmployeeSummary bindVariableInFunctionParameters(Employee employee, User user);

  @Select
  List<Employee> bindVariableForItemHasNextAndIndex(List<Employee> employees);

  @Select
  Project optionalDaoParameterFieldAccess(Optional<Project> project, OptionalInt id, OptionalLong longId, OptionalDouble doubleId);

  @Select
  EmployeeSummary implementCustomFunctions(EmployeeSummary employee);

  @Select
  EmployeeSummary invalidImplementCustomFunctions(EmployeeSummary employee);
}