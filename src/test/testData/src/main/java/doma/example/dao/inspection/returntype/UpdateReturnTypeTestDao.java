package doma.example.dao.inspection.returntype;

import java.util.Optional;
import org.seasar.doma.*;
import org.seasar.doma.jdbc.Result;
import doma.example.entity.*;
import java.util.List;

@Dao
public interface UpdateReturnTypeTestDao {
    @Insert
    int insertReturnsInt(Packet e);

    @Update
    String <error descr="The return type must be \"int\"">updateReturnsString</error>(Packet e);

    @Delete(returning = @Returning)
    Packet deleteReturningEntity(Packet e);

    @Delete(returning = @Returning)
    Optional<Packet> deleteReturningOptionalEntity(Packet e);

    @Delete(returning = @Returning)
    int <error descr="When \"returning = @Returning\" is specified, the return type must be \"Packet\" or Optional<Packet>">deleteReturningInt</error>(Packet e);

    @Update
    Result<Pckt> updateReturnsResultWithImmutable(Pckt e);

    @Update(sqlFile = true)
    int <error descr="If a method annotated with @Update targets immutable entities for insertion, the return type must be Result<Pckt>">deleteImmutableInt</error>(Pckt e);

    @Update
    int <error descr="If a method annotated with @Update targets immutable entities for insertion, the return type must be Result<Pckt>">updateReturnsIntWithImmutable</error>(Pckt e);
}

