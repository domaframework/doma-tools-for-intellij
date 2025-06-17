package doma.example.dao.inspection.paramtype;

import doma.example.entity.*;
import org.seasar.doma.*;
import org.seasar.doma.jdbc.BatchResult;

import java.util.List;
import java.util.Optional;

@Dao
public interface BatchInsertUpdateDeleteParamTestDao {

    @BatchInsert
    int[] batchInsertWrapper(List<Integer> <error descr="The argument must be an Iterable subclass that has an Entity class as a parameter">ids</error>);

    @BatchUpdate
    int[] batchInsertEmpNotList(Pckt <error descr="The argument must be an Iterable subclass that has an Entity class as a parameter">pckt</error>);

    @BatchInsert
    @Sql("insert into values (/*id*/0)")
    int[] batchInsertWithSqlAnnotation(Integer <error descr="The argument must be an Iterable subclass that has an Entity class as a parameter">id</error>);

    @BatchUpdate
    int[] <error descr="The number of parameters must be one">batchInsertEmpNotList</error>(List<Pckt> pckts,String name);

    @BatchUpdate
    int[] batchInsertEmp(List<Pckt> pckts);

    @BatchUpdate
    BatchResult<Pckt> batchInsertEmpResult(List<Pckt> pckts);

    @BatchUpdate
    BatchResult<Packet> batchInsertResult(List<Packet> packets);

    @BatchUpdate
    int[] batchInsertOptional(List<Optional<Packet>> <error descr="The argument must be an Iterable subclass that has an Entity class as a parameter">packets</error>);

    @BatchUpdate
    @Sql("insert into values (/*packets.id*/0, /*packets.name*/'name')")
    BatchResult<List<Packet>> batchInsertResultWithSql(List<Optional<Packet>> <error descr="The argument must be an Iterable subclass that has an Entity class as a parameter">packets</error>);

    @BatchUpdate
    @Sql("insert into values (/*packets.id*/0, /*packets.name*/'name')")
    int[] batchInsertWithSql(List<Optional<Packet>> <error descr="The argument must be an Iterable subclass that has an Entity class as a parameter">packets</error>);

    @BatchUpdate
    int[] batchInsert(List<Packet> packets);
}

