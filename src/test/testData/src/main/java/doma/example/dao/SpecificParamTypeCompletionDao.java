package doma.example.dao;

import doma.example.collector.HogeCollector;
import doma.example.entity.*;
import doma.example.function.HogeFunction;
import doma.example.option.HogeSelectOptions;
import org.seasar.doma.*;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;
import org.seasar.doma.jdbc.SelectOptions;
import doma.example.function.HogeBiFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

@Dao
public interface SpecificParamTypeCompletionDao {

  /**
   * SelectOptions do not cause errors even when unused
   * @param searchName
   * @param options
   * @return
   */
  @Select
  Project selectSelectOption(Integer id, String searchName, SelectOptions options);

  /**
   * SelectOptions subtypes cause errors even when unused
   * @param searchName
   * @param hogeSelectOptions
   * @return
   */
  @Select
  Project selectSubTypeSelectOption(Employee employee,
      String searchName,
      HogeSelectOptions hogeSelectOptions);

  /**
   * Collector do not cause errors even when unused in SQL
   * @param salary
   * @param collector
   * @return
   */
  @Select(strategy = SelectType.COLLECT)
  Pckt selectCollector(BigDecimal salary, Collector<Packet, BigDecimal, Pckt> collector);

  /**
   * Collector subclasses do not cause errors even when unused in SQL
   * @param employee
   * @param id
   * @param hogeCollector
   * @return
   */
  @Select(strategy = SelectType.COLLECT)
  String selectSubTypeCollector(Employee employee,
      Integer id,
      HogeCollector hogeCollector);

  /**
   * Function parameters do not cause errors even when unused
   * @param employee
   * @param id
   * @param function
   * @return
   */
  @Select(strategy = SelectType.STREAM)
  String selectFunction(Employee employee,
      Integer id,
      Function<Stream<Employee>, String> function);

  /**
   * Function subtypes do not cause errors even when unused
   * @param employee
   * @param id
   * @param function
   * @return
   */
  @Select(strategy = SelectType.STREAM)
  String selectSubTypeFunction(Employee employee,
      Integer id,
      HogeFunction function);

  /**
   * BiFunction parameters do not cause errors even when unused
   * @param tableName
   * @param func
   * @return
   */
  @SqlProcessor
  Pckt executeBiFunction(String tableName, BiFunction<Config, PreparedSql, Pckt> func);

  /**
   * BiFunction subtypes do not cause errors even when unused
   * @param tableName
   * @param func
   * @return
   */
  @SqlProcessor
  String executeSubTypeBiFunction(String tableName, HogeBiFunction func);

}
