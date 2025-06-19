package doma.example.dao.inspection.paramtype;

import org.seasar.doma.*;
import org.seasar.doma.message.Message;
import doma.example.entity.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Stream;
import java.util.function.Function;

@Dao
public interface SelectParamTestDao {

  @Select
  Pckt selectPcktById(Integer id);

  @Select
  @Sql("SELECT * FROM packet WHERE id = /* pckt.id */0")
  Pckt selectEmp(Pckt pckt);

  @Select(strategy = SelectType.STREAM)
  @Sql("Select 10000 from user where name = /* name */'name' and salary = /* salary */0")
  Stream<Packet> <error descr="When you specify SelectType.STREAM for the strategy element of @Select, the \"java.util.function.Function<java.util.stream.Stream>\" parameter is required for the method">selectReturnStreamWithStreamOption</error>(String name, BigDecimal salary);

  @Select
  @Sql("Select 10000 from user")
  Stream<Packet> <error descr="When you use the \"java.util.function.Function\" parameter, SelectStrategyType.STREAM must be specified for the strategy element of @Select">selectReturnStreamWithOutStreamOption</error>(Function<Stream<Packet>, BigDecimal> streams);

  @Select(strategy = SelectType.STREAM)
  @Sql("Select 10000 from user")
  Integer selectReturnStream(Function<Stream<Pckt>, BigDecimal> stream);

  @Select(strategy = SelectType.STREAM)
  @Sql("Select 10000 from user")
  BigDecimal selectStream(Function<Stream<Pckt>, BigDecimal> stream);

  @Suppress(messages = { Message.DOMA4274 })
  @Select
  @Sql("Select 10000 from user where name = /* name */'name' and salary = /* salary */0")
  Stream<Package> selectReturnStream(String name, BigDecimal salary);

  @Select
  @Sql("select * from packet where salary > /* salary */0")
  Pckt selectCollectNotOption(BigDecimal salary, Collector<Packet, BigDecimal, Pckt> collector);

  @Select(strategy = SelectType.COLLECT)
  @Sql("select * from packet where salary > /* salary */0")
  Pckt <error descr="When you specify SelectType.COLLECT for the strategy element of @Select, the \"java.util.stream.Collector\" parameter is required for the method">selectWithOutCollector</error>(BigDecimal salary, Packet packet);

  @Select(strategy = SelectType.COLLECT)
  @Sql("select * from emp where salary > /* salary */0")
  Optional<Packet> selectCollectInValidParamResult(BigDecimal salary,Collector<Packet, ?, Optional<Packet>> collector);

  @Select(strategy = SelectType.COLLECT)
  @Sql("select * from emp where salary > /* salary */0")
  Pckt selectCollectAccumulation(BigDecimal salary, Collector<Packet, BigDecimal, Pckt> collector);
}

