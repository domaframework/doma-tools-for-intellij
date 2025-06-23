package doma.example.dao.inspection.returntype;

import org.seasar.doma.*;
import org.seasar.doma.jdbc.BatchResult;
import doma.example.entity.*;
import java.util.List;

@Dao
public interface BatchReturnTypeTestDao {
    @BatchInsert
    int[] batchInsertReturnsIntArray(List<Packet> e);

    @BatchUpdate
    String <error descr="The return type must be \"int[]\"">batchUpdateReturnsString</error>(List<Packet> e);

    @BatchUpdate(sqlFile = true)
    int[] <error descr="If a method annotated with @BatchUpdate targets immutable entities for insertion, the return type must be BatchResult<Pckt>">batchUpdateImmutableReturnsIntArray</error>(List<Pckt> e);

    @BatchDelete
    int[] <error descr="If a method annotated with @BatchDelete targets immutable entities for insertion, the return type must be BatchResult<Pckt>">batchDeleteReturnsIntWithImmutable</error>(List<Pckt> e);

    @BatchDelete
    BatchResult<Pckt> batchDeleteReturnsBatchResultWithImmutable(List<Pckt> e);
}
