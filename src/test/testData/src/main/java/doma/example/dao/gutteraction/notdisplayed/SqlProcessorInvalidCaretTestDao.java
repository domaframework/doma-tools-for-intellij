package doma.example.dao.gutteraction;

import org.seasar.doma.Dao;
import org.seasar.doma.SqlProcessor;
import org.seasar.doma.Sql;
import org.seasar.doma.jdbc.PreparedSql;

import org.seasar.doma.jdbc.Config;
import java.util.function.BiFunction;

@Dao
interface SqlProcessorInvalidCaretTestDao {

  @SqlProcessor
  <R> R existsSQLFile1(Integer id, BiFunction<Config, PreparedSql, R> handler);



  @Sql("select * from employee where id = /* id */0")
  @SqlProcessor
  <R> R nonExistSQLFile(Integer id, BiFunction<Config, PreparedSql, R> handler);

  @SqlProcessor
  <R> R NotSQL<caret>ExistError(Integer id, BiFunction<Config, PreparedSql, R> handler);
}