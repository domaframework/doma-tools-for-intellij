package doma.example.dao.inspection.paramtype;

import org.seasar.doma.*;
import org.seasar.doma.jdbc.Reference;
import doma.example.entity.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Dao
public interface ProcedureParamTestDao {

    @Procedure
    void executeNotAnnotation(@In Integer inParam,@InOut Reference<String> inOurPatam, @Out Reference<Integer> outParam);

    @Procedure
    void executeValidType(@In Reference<Integer> <error descr="Reference<Integer> is not supported as the type of the parameter annotated with @In">inParam</error>
        , @InOut Integer <error descr="The parameter type annotated with @InOut must be \"org.seasar.doma.jdbc.Reference\"">inOurPatam</error>
        , @Out String <error descr="The parameter type annotated with @Out must be \"org.seasar.doma.jdbc.Reference\"">outParam</error>
        , @ResultSet Optional<Pckt> <error descr="The parameter type annotated with @ResultSet must be \"java.util.List\"">optional</error>);

    @Function
    int executeFuncValidType(@Out Reference<Optional<String>> out,@ResultSet List<Optional<Packet>> optional);
}

