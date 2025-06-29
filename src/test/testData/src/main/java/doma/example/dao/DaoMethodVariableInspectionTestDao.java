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
import java.util.stream.Stream;
import java.util.function.Function;
import java.util.stream.Collector;
import doma.example.function.*;
import doma.example.collector.*;
import doma.example.option.*;

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
  @Sql("select * from employee where name = /* name */'test'")
  Employee noArgumentsUsedInSQLAnnotations(String name,Integer <error descr="There are unused parameters in the SQL [id]">id</error>);

  @SqlProcessor
  <R> R biFunctionDoesNotCauseError(Integer id, BiFunction<Config, PreparedSql, R> handler);

  @SqlProcessor
  @Sql("SELECT id, name FROM demo")
  <R> R biFunctionHogeFunction(HogeBiFunction handler);

  @Select
  Project selectOptionDoesNotCauseError(Employee <error descr="There are unused parameters in the SQL [employee]">employee</error>,
      String searchName,
      SelectOptions options);

  @Select
  @Sql("SELECT * FROM project WHERE name = /* searchName */'test'")
  Project selectHogeOption(Employee <error descr="There are unused parameters in the SQL [employee]">employee</error>,
      String searchName,
      HogeSelectOptions options);

  @Select(strategy = SelectType.COLLECT)
  Project collectDoesNotCauseError(Employee <error descr="There are unused parameters in the SQL [employee]">employee</error>,
      Integer id,
      Collector<Project, ?, Project> collector);

  @Select(strategy = SelectType.COLLECT)
  Project selectHogeCollector(Employee <error descr="There are unused parameters in the SQL [employee]">employee</error>,
      Integer id,
      HogeCollector collector);

  @Select
  Project collectDoesCauseError(Employee <error descr="There are unused parameters in the SQL [employee]">employee</error>,
      String searchName,
      Collector<Project, ?, Project> collector);

  @Select(strategy = SelectType.STREAM)
  String functionDoesNotCauseError(Employee <error descr="There are unused parameters in the SQL [employee]">employee</error>,
      Integer id,
      Function<Stream<Employee>, String> function);

  @Select(strategy = SelectType.STREAM)
  String selectHogeFunction(Employee <error descr="There are unused parameters in the SQL [employee]">employee</error>,
      Integer id,
      HogeFunction function);

  @Select
  Project noErrorWhenUsedInFunctionParameters(Employee employee, Integer count);

  @Select
  Employee duplicateForDirectiveDefinitionNames(Employee <error descr="An element name that is a duplicate of an element name defined in SQL is used">member</error>,
      Integer <error descr="There are unused parameters in the SQL [count]">count</error>,
      List<Employee> users,
      String searchName,
      Boolean inForm);

}