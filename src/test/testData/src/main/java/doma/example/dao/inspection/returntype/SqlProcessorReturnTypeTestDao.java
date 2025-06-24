package doma.example.dao.inspection.returntype;

import org.seasar.doma.Dao;
import org.seasar.doma.SqlProcessor;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;

import java.util.function.BiFunction;
import doma.example.entity.*;
import doma.example.function.*;
import java.util.Optional;

@Dao
public interface SqlProcessorReturnTypeTestDao {
    @SqlProcessor
    <R> R processSqlReturnsR(BiFunction<Config, PreparedSql, R> handler);

    @SqlProcessor
    String <error descr="The return type \"java.lang.String\" is not the same as the third type argument \"java.lang.Integer\" of BiFunction">processSqlReturnsString</error>(BiFunction<Config, PreparedSql, Integer> handler);

    @SqlProcessor
    Optional<String> <error descr="The return type \"java.util.Optional<java.lang.String>\" is not the same as the third type argument \"doma.example.entity.Pckt\" of BiFunction">processSqlReturnsOptional</error>(BiFunction<Config, PreparedSql, Pckt> handler);

    @SqlProcessor
    void processSqlReturnsVoid(BiFunction<Config, PreparedSql, Void> handler);

    @SqlProcessor
    String <error descr="The return type must be \"void\"">processSqlReturnsStringWithVoidHandler</error>(BiFunction<Config, PreparedSql, Void> handler);

    @SqlProcessor
    void <error descr="The return type \"void\" is not the same as the third type argument \"java.lang.String\" of BiFunction">processSqlHogeBiFunctionInvalidReturnVoid</error>(HogeBiFunction handler);

    @SqlProcessor
    String processSqlHogeBiFunction(HogeBiFunction handler);

    @SqlProcessor
    Integer <error descr="The return type \"java.lang.Integer\" is not the same as the third type argument \"java.lang.String\" of BiFunction">processSqlHogeBiFunctionInvalidReturn</error>(HogeBiFunction handler);
}
