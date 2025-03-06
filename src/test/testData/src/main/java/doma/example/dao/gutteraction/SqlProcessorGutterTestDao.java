package doma.example.dao.gutteraction;

import org.seasar.doma.Dao;
import org.seasar.doma.SqlProcessor;
import org.seasar.doma.Sql;
import org.seasar.doma.jdbc.PreparedSql;

import org.seasar.doma.jdbc.Config;
import java.util.function.BiFunction;

@Dao
interface SqlProcessorGutterTestDao {
  
  @SqlProcessor
  <R> R existsSQL<caret>File1(Integer id, BiFunction<Config, PreparedSql, R> handler);
  
  @SqlProcessor
  <R> R <error descr="\"SQL file does not exist\"">nonExistSQLFileError</error>(Integer id, BiFunction<Config, PreparedSql, R> handler);

  @Sql("select * from employee where id = /* id */0")
  @SqlProcessor
  <R> R nonExistSQLFile(Integer id, BiFunction<Config, PreparedSql, R> handler);

  @SqlProcessor
  <R> R existsSQLFile2(Integer id, BiFunction<Config, PreparedSql, R> handler);
}