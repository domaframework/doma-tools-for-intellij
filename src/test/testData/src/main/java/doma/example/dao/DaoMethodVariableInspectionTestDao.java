package doma.example.dao;

import doma.example.entity.*;
import org.seasar.doma.BatchDelete;
import org.seasar.doma.BatchInsert;
import org.seasar.doma.BatchUpdate;
import org.seasar.doma.Dao;
import org.seasar.doma.Delete;
import org.seasar.doma.Insert;
import org.seasar.doma.MultiInsert;
import org.seasar.doma.Script;
import org.seasar.doma.Select;
import org.seasar.doma.Sql;
import org.seasar.doma.SqlProcessor;
import org.seasar.doma.Update;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.SelectOptions;
import org.seasar.doma.SelectType;
import java.util.stream.Collector;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Test to check for unused arguments in SQL
 */
@Dao
interface DaoMethodVariableInspectionTestDao {

  @Select
  List<Employee> nonExistSQLFile(String name);

  @Select
  @Sql("select * from employee where name = /* employee.employeeName */'test'")
  Employee noArgumentsUsedInSQLAnnotations(Employee employee,String <error descr="There are unused parameters in the SQL [employeeName]">employeeName</error>);

  @SqlProcessor
  <R> R biFunctionDoesNotCauseError(Integer id, BiFunction<Config, PreparedSql, R> handler);

  @Select
  Project selectOptionDoesNotCauseError(Employee <error descr="There are unused parameters in the SQL [employee]">employee</error>,String searchName,SelectOptions options);

  @Select(strategy = SelectType.COLLECT)
  Project collectDoesNotCauseError(Employee <error descr="There are unused parameters in the SQL [employee]">employee</error>,Integer id,Collector<Project, ?, Project> collector);

  @Select
  Project collectDoesCauseError(Employee <error descr="There are unused parameters in the SQL [employee]">employee</error>,String searchName,Collector<Project, ?, Project> <error descr="There are unused parameters in the SQL [collector]">collector</error>);

  @Select
  Project noErrorWhenUsedInFunctionParameters(Employee employee, Integer count);

  @Select
  Employee duplicateForDirectiveDefinitionNames(Employee <error descr="An element name that is a duplicate of an element name defined in SQL is used">member</error>, Integer <error descr="There are unused parameters in the SQL [count]">count</error>,
      List<Employee> users,
      String searchName,
      Boolean inForm);

}