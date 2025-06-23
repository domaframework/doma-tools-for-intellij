package doma.example.dao.inspection.returntype;

import java.math.BigDecimal;
import org.seasar.doma.*;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;
import org.seasar.doma.*;
import org.seasar.doma.jdbc.SelectOptions;
import org.seasar.doma.message.Message;
import doma.example.entity.*;
import doma.example.domain.*;
import doma.example.collector.*;

@Dao
public interface SelectReturnTypeTestDao {

  @Select
  void <error descr="The return type void is invalid">selectVoid</error>(Employee e);

  @Select
  List<Employee> selectByExample(Employee e);

  @Select
  List<Employee> selectWithOptionalOrderBy(String employeeName, String orderBy);

  @Select
  Employee selectById(Integer employeeId);

  @Select
  Employee selectById(Integer employeeId,SelectOptions options);

  @Sql("select * from EMPLOYEE where EMPLOYEE_NAME in /*names*/('aaa', 'bbb')")
  @Select
  List<Employee> selectByNames(List<String> names);

  @Select(mapKeyNaming = MapKeyNamingType.CAMEL_CASE)
  Map<String, Object> selectByIdAsMap(Integer employeeId);

  @Select(mapKeyNaming = MapKeyNamingType.CAMEL_CASE)
  List<Map<String, Object>> selectAllAsMapList();

  @Select(strategy = SelectType.STREAM, mapKeyNaming = MapKeyNamingType.CAMEL_CASE)
  <R> R selectAllAsMapList(Function<Stream<Map<String, Object>>, R> mapper);

  @Select(strategy = SelectType.STREAM, mapKeyNaming = MapKeyNamingType.CAMEL_CASE)
  <R> R selectAllAsMapList(Function<Stream<Map<String, Object>>, R> mapper,SelectOptions options);

  @Select(strategy = SelectType.STREAM, mapKeyNaming = MapKeyNamingType.CAMEL_CASE)
  String <error descr="The return type must match RESULT of the \"java.util.function.Function\" type parameter">selectStreamInvalid</error>(Function<Stream<Map<String, Object>>, Integer> mapper,SelectOptions options);

  @Select
  List<Employee> selectByNamePrefix(String employeeName);

  @Select
  List<Employee> selectByNameInfix(String employeeName);

  @Select
  List<Employee> selectByNameSuffix(String employeeName);

  @Select
  List<Employee> selectAll();

  @Select
  List<Employee> selectAll(SelectOptions options);

  @Select
  List<Employee> selectDistinctAll(SelectOptions options);

  @Select(ensureResultMapping = true)
  List<Employee> selectOnlyNameWithMappingCheck();

  @Select(ensureResultMapping = false)
  List<Employee> selectOnlyNameWithoutMappingCheck();

  @Select
  List<Employee> selectByInterface(Hiredate hiredate);

  @Select(strategy = SelectType.STREAM)
  <R> R streamAll(Function<Stream<Employee>, R> mapper);

  @Select(strategy = SelectType.STREAM)
  <R> R streamAll(Function<Stream<Employee>, R> mapper,SelectOptions options);

  @Select(strategy = SelectType.STREAM)
  <R> R streamAllSalary(Function<Stream<BigDecimal>, R> mapper);

  @Select(strategy = SelectType.STREAM)
  <R> R streamAllSalary(Function<Stream<BigDecimal>, R> mapper,SelectOptions options);

  @Select(strategy = SelectType.STREAM)
  <R> R streamBySalary(BigDecimal salary, Function<Stream<Employee>, R> mapper);

  @Select(strategy = SelectType.COLLECT)
  <R> R collectAll(Collector<Employee, ?, R> collector);

  @Select
  @Suppress(messages = {Message.DOMA4274})
  Stream<Employee> streamAll();

  @Select
  @Suppress(messages = {Message.DOMA4274})
  Stream<Employee> streamAll(SelectOptions options);

  @Select
  @Suppress(messages = {Message.DOMA4274})
  Stream<Employee> streamBySalary(BigDecimal salary);

  @Select(strategy = SelectType.COLLECT)
  Integer selectCollector(Integer id, String name, Collector<Pckt, ?, Integer> collector);

  @Select(strategy = SelectType.COLLECT)
  <R> R selectCollectorR(Integer id, Collector<Packet, ?, R> collector);

  @Select(strategy = SelectType.COLLECT)
  Pckt selectWithOutCollector(BigDecimal salary, Packet packet);

  @Select(strategy = SelectType.COLLECT)
  <R extends Number> R select(Collector<String, ?, R> collector);

  @Select(strategy = SelectType.COLLECT)
  String selectWithHogeCollector(HogeCollector collector);

  @Select(strategy = SelectType.COLLECT, mapKeyNaming = MapKeyNamingType.CAMEL_CASE)
  <R> R selectByIdAsMap(Integer id, Collector<Map<String, Object>, ?, R> collector);


}
