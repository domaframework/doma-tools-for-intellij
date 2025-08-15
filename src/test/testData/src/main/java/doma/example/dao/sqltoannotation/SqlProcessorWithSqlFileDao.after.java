package doma.example.dao.sqltoannotation;

import org.seasar.doma.Dao;
import org.seasar.doma.Sql;
import org.seasar.doma.SqlProcessor;

import java.util.function.BiFunction;

@Dao
public interface SqlProcessorWithSqlFileDao {
    @SqlProcessor
    @Sql("""
            SELECT count(*)
              FROM employee
             WHERE age > 25
            """)
    <R> R processData(BiFunction<Sql<?>, Sql<?>, R> processor);
}