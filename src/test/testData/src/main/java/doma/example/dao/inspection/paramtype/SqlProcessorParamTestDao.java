package doma.example.dao.inspection.paramtype;

import org.seasar.doma.*;
import doma.example.entity.*;
import java.util.function.BiFunction;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;

@Dao
public interface SqlProcessorParamTestDao {

    @SqlProcessor
    @Sql("")
    <R> R <error descr="When you annotate the method with @SqlProcessor, the BiFunction parameter is required for the method">executeNotBiFunction</error>(Integer id);

    @SqlProcessor
    @Sql("")
    <R> R executeBiFunctionNotConfig(BiFunction<String,PreparedSql, R> <error descr="The first type argument of BiFunction must be \"org.seasar.doma.jdbc.Config\"">func</error>);

    @SqlProcessor
    <R> R executeBiFunctionNotPreparedSql(BiFunction<Config,String, R> <error descr="The second type argument of BiFunction must be \"org.seasar.doma.jdbc.PreparedSql\"">func</error>);
    @SqlProcessor
    <R> R executeR(Integer id,BiFunction<Config, PreparedSql, Pckt> func);

    @SqlProcessor
    Integer executeInteger(Integer id,BiFunction<Config, PreparedSql, Pckt> func);

    @SqlProcessor
    Pckt executePckt(BiFunction<Config, PreparedSql, Pckt> func);

    @SqlProcessor
    <R> R execute(BiFunction<Config, PreparedSql, R> func);
}

