package doma.example.dao.sqltoannotation;

import org.seasar.doma.Dao;
import org.seasar.doma.SqlProcessor;

import java.util.function.BiFunction;

@Dao
public interface SqlProcessorWithSqlFileDao {
    @SqlProcessor
    <R> R processData(BiFunction<Sql<?>, Sql<?>, R> processor);
}