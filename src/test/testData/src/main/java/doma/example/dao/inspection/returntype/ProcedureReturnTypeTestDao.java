package doma.example.dao.inspection.returntype;

import org.seasar.doma.*;

@Dao
public interface ProcedureReturnTypeTestDao {
    @Procedure
    void callProcedureReturnsVoid();
    @Procedure
    int <error descr="The return type must be \"void\"">callProcedureReturnsInt</error>();
}