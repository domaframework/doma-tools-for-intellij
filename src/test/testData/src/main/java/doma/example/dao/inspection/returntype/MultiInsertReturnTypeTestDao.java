package doma.example.dao.inspection.returntype;

import org.seasar.doma.*;
import org.seasar.doma.jdbc.MultiResult;
import doma.example.entity.*;
import java.util.List;

@Dao
public interface MultiInsertReturnTypeTestDao {
    @MultiInsert
    int multiInsertReturnsInt(List<Packet> e);

    @MultiInsert
    String <error descr="If a method annotated with @MultiInsert targets immutable entities for insertion, the return type must be MultiResult<Pckt>">multiInsertReturnsString</error>(List<Pckt> e);

    @MultiInsert(returning = @Returning)
    List<Packet> multiInsertReturningList(List<Packet> e);

    @MultiInsert(returning = @Returning)
    String <error descr="When \"returning = @Returning\" is specified, the return type must be List<Packet>">multiInsertReturningString</error>(List<Packet> e);

    @MultiInsert
    int <error descr="If a method annotated with @MultiInsert targets immutable entities for insertion, the return type must be MultiResult<Pckt>">multiInsertReturnsIntWithImmutable</error>(List<Pckt> e);

    @MultiInsert
    MultiResult<Pckt> multiInsertReturnsMultiResultWithImmutable(List<Pckt> e);
}
