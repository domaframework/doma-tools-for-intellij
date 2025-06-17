package doma.example.dao.inspection.paramtype;

import org.seasar.doma.*;
import org.seasar.doma.jdbc.Reference;

import java.util.List;
import java.util.Map;

@Dao
public interface ProcedureParamTestDao {

    @Procedure
    void executeNotAnnotation(@In Integer inParam,@InOut Reference<String> inOurPatam, @Out Reference<Integer> outParam);

    @Procedure
    void executeValidType(@In Reference<Integer> <error descr="Reference<Integer> is not supported as the type of the parameter annotated with @In">inParam</error>,@InOut Integer <error descr="The parameter type annotated with @InOut must be \"org.seasar.doma.jdbc.Reference\"">inOurPatam</error>, @Out String <error descr="The parameter type annotated with @Out must be \"org.seasar.doma.jdbc.Reference\"">outParam</error>,@ResultSet List<Optional<<error descr="Cannot resolve symbol 'Emp'">Emp</error>>> <error descr="\"Optional<Emp>\" is illegal as the type argument of \"java.util.List\"">optional</error>);

    @Function
    int executeFuncValidType(@Out Reference<<error descr="Cannot resolve symbol 'Optional'">Optional</error><String>> <error descr="\"Optional<String>\" is illegal as the type argument of \"org.seasar.doma.jdbc.Reference\"">out</error>,@ResultSet List<Optional<<error descr="Cannot resolve symbol 'Address'">Address</error>>> <error descr="\"Optional<Address>\" is illegal as the type argument of \"java.util.List\"">optional</error>);
}

