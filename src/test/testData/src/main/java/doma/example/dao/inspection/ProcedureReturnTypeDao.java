package doma.example.dao.inspection;

import org.seasar.doma.*;

@Dao
public interface ProcedureReturnTypeDao {
    @Procedure
    void callProcedureReturnsVoid();
    @Procedure
    int <error descr="The return type must be \"void\"">callProcedureReturnsInt</error>();
}