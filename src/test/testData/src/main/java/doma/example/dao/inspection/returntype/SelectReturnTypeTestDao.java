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
import doma.example.function.*;

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
  String <error descr="The return type must match the \"java.lang.Integer\" type parameter \"java.util.function.Function\"">selectStreamInvalid</error>(Function<Stream<Map<String, Object>>, Integer> mapper,SelectOptions options);

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

  @Select(strategy = SelectType.COLLECT, mapKeyNaming = MapKeyNamingType.CAMEL_CASE)
  <R> R selectByIdAsMap(Integer id, Collector<Map<String, Object>, ?, R> collector);

  @Select(strategy = SelectType.COLLECT)
  String selectWithHogeCollector(HogeCollector collector);

  @Select(strategy = SelectType.COLLECT)
  @Sql("select * from emp where salary > /* salary */0")
  Pckt <error descr="The return type must match the \"java.lang.String\" type parameter \"java.util.stream.Collector\"">selectHogeCollectInvaliReturn</error>(BigDecimal salary, HogeCollector collector);

  @Select(strategy = SelectType.STREAM)
  @Sql("select * from emp where salary > /* salary */0")
  Integer <error descr="The return type must match the \"java.lang.String\" type parameter \"java.util.function.Function\"">selectHogeFunctionInvalidReturn</error>(BigDecimal salary, HogeFunction function);

  @Select(strategy = SelectType.STREAM)
  @Sql("select * from emp where salary > /* salary */0")
  String selectHogeFunction(BigDecimal salary, HogeFunction function);

  // Test cases for primitive return types - these should be valid (except char)
  @Select
  @Sql("select count(*) from EMPLOYEE")
  int selectCountAsInt();

  @Select
  @Sql("select count(*) from EMPLOYEE")
  long selectCountAsLong();

  @Select
  @Sql("select exists(select 1 from EMPLOYEE where EMPLOYEE_ID = /* id */1)")
  boolean selectExistsAsBoolean(Integer id);

  @Select
  @Sql("select avg(SALARY) from EMPLOYEE")
  double selectAverageAsDouble();

  @Select
  @Sql("select avg(SALARY) from EMPLOYEE")
  float selectAverageAsFloat();

  @Select
  @Sql("select age from EMPLOYEE where EMPLOYEE_ID = /* id */1")
  byte selectAgeAsByte(Integer id);

  @Select
  @Sql("select department_id from EMPLOYEE where EMPLOYEE_ID = /* id */1")
  short selectDepartmentIdAsShort(Integer id);

  // This should show an error - char is not supported
  @Select
  @Sql("select initial from EMPLOYEE where EMPLOYEE_ID = /* id */1")
  char <error descr="The return type char is invalid">selectInitialAsChar</error>(Integer id);

}
