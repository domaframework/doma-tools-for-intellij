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

  @Select
  Project selectOptionDoesNotCauseError(Employee <error descr="There are unused parameters in the SQL [employee]">employee</error>,String searchName,SelectOptions options);

}