package doma.example.dao.inspection.paramtype;

import doma.example.entity.*;
import org.seasar.doma.*;
import org.seasar.doma.jdbc.MultiResult;

import java.util.List;

@Dao
public interface MultiInsertParamTestDao {

    @MultiInsert
    int multiInsertNotList(Pckt <error descr="The argument must be an Iterable subclass that has an Entity class as a parameter">Pckt</error>);

    @MultiInsert
    int <error descr="The number of parameters must be one">multiInsertAnyParams</error>(List<Packet> PcktList, String name);

    @MultiInsert
    int multiInsertNotEntityIterable(List<Integer> <error descr="The argument must be an Iterable subclass that has an Entity class as a parameter">PcktList</error>);

    @MultiInsert
    int[] multiInsert(List<Pckt> PcktList);

    @MultiInsert
    MultiResult<User> multiInsertNoMatchResult(List<Pckt> PcktList);

    @MultiInsert
    MultiResult<Pckt> multiInsertResult(List<Pckt> PcktList);

    @MultiInsert
    MultiResult<Packet> multiInsertPacket(List<Packet> PacketList);

    @MultiInsert(returning = @Returning)
    int[] multiInsertReturning(List<Pckt> Pckts);

    @MultiInsert(returning = @Returning)
    List<Pckt> multiInsertReturningList(List<Pckt> Pckts);
}
