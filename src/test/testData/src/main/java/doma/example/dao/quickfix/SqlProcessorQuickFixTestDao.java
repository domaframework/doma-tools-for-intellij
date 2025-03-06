package doma.example.dao.quickfix;

import org.seasar.doma.Dao;
import org.seasar.doma.SqlProcessor;
import org.seasar.doma.Sql;
import org.seasar.doma.jdbc.PreparedSql;

import org.seasar.doma.jdbc.Config;
import java.util.function.BiFunction;

@Dao
interface SqlProcessorQuickFixTestDao {

  @SqlProcessor
  <R> R existsSQLFile(Integer id, BiFunction<Config, PreparedSql, R> handler);

  @SqlProcessor
  <R> R generateSQLFile<caret>(Integer id, BiFunction<Config, PreparedSql, R> handler);
  
  @Sql("select * from employee where id = /* id */0")
  @SqlProcessor
  <R> R nonExistSQLFile(Integer id, BiFunction<Config, PreparedSql, R> handler);
}