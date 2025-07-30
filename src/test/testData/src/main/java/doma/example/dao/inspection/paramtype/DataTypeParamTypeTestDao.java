package doma.example.dao.inspection.paramtype;

import org.seasar.doma.*;
import doma.example.domain.Salary;
import org.seasar.doma.jdbc.Reference;
import org.seasar.doma.jdbc.Result;

import java.util.List;
import java.util.Optional;

@Dao
public interface DataTypeParamTypeTestDao {

  @Select
  @Sql("select * from salary where id = /*salary.value*/0")
  Salary selectSalary(Salary salary);

  @Select
  @Sql("select * from salary where id = /*salary.value*/0")
  Optional<Salary> selectOptSalary(Salary salary);

  @Update
  int updateSalary(Salary <error descr="The parameter type must be an entity">salary</error>);

  @Insert
  int insertSalary(Salary <error descr="The parameter type must be an entity">salary</error>);

  @Delete
  int deleteSalary(Salary <error descr="The parameter type must be an entity">salary</error>);

  @BatchUpdate
  int[] batchUpdateSalary(List<Salary> <error descr="The argument must be an Iterable subclass that has an Entity class as a parameter">salary</error>);

  @BatchUpdate
  int[] batchInsertSalary(List<Salary> <error descr="The argument must be an Iterable subclass that has an Entity class as a parameter">salary</error>);

  @BatchUpdate
  int[] batchDeleteSalary(List<Salary> <error descr="The argument must be an Iterable subclass that has an Entity class as a parameter">salary</error>);

  @Update
  @Sql("UPDATE salary SET val = /*salary.value*/0")
  int updateSalaryWithSql(Salary salary);

  @Insert
  @Sql("INSERT INTO salary (val) VALUES (/*salary.value*/0)")
  int insertSalaryWithSql(Salary salary);

  @Delete
  @Sql("DELETE FROM salary WHERE val = /*salary.value*/0 ")
  int deleteSalaryWithSql(Salary salary);

  @BatchUpdate
  @Sql("UPDATE salary SET val = /*salary.value*/0")
  int[] batchUpdateSalaryWithSql(List<Salary> salary);

  @BatchUpdate
  @Sql("INSERT INTO salary (val) VALUES (/*salary.value*/0)")
  int[] batchInsertSalaryWithSql(List<Salary> salary);

  @BatchUpdate
  @Sql("DELETE FROM salary WHERE val = /*salary.value*/0 ")
  int[] batchDeleteSalaryWithSql(List<Salary> salary);

  @Function
  List<Salary> getTopSalaries(@In Salary limit);

  @Function
  Optional<Salary> getMaxSalary(@InOut Reference<Salary> percentage, @ResultSet List<Salary> resultSet);

  @Procedure
  void computeBonus(@In Salary employeeId);

  @Procedure
  void adjustSalaries(@InOut Reference<Salary> percentage, @ResultSet List<Salary> resultSet);

}