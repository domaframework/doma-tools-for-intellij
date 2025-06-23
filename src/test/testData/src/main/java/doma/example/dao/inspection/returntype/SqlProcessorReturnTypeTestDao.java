package doma.example.dao.inspection.returntype;

import org.seasar.doma.Dao;
import org.seasar.doma.SqlProcessor;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;

import java.util.function.BiFunction;
import doma.example.entity.*;

@Dao
public interface SqlProcessorReturnTypeTestDao {
    @SqlProcessor
    <R> R processSqlReturnsR(BiFunction<Config, PreparedSql, R> handler);

    @SqlProcessor
    String <error descr="The return type \"java.lang.String\" is not the same as the third type argument \"java.lang.Integer\" of BiFunction">processSqlReturnsString</error>(BiFunction<Config, PreparedSql, Integer> handler);

    @SqlProcessor
    void processSqlReturnsVoid(BiFunction<Config, PreparedSql, Void> handler);

    @SqlProcessor
    String <error descr="The return type must be \"void\"">processSqlReturnsStringWithVoidHandler</error>(BiFunction<Config, PreparedSql, Void> handler);
}
