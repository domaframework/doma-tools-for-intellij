package doma.example.dao;

import doma.example.entity.*;
import org.seasar.doma.*;

import org.seasar.doma.jdbc.PreparedSql;

import org.seasar.doma.jdbc.Config;
import java.util.function.BiFunction;

@Dao
interface SqlProcessorTestDao {

  @SqlProcessor
  <R> R existsSQLFile(Integer id, BiFunction<Config, PreparedSql, R> handler);

  @SqlProcessor
  <R> R <error descr="SQL file does not exist">nonExistSQLFileError</error>(Integer id, BiFunction<Config, PreparedSql, R> handler);

  @Sql("select * from employee where id = /* id */0")
  @SqlProcessor
  <R> R nonExistSQLFile(Integer id, BiFunction<Config, PreparedSql, R> handler);
}