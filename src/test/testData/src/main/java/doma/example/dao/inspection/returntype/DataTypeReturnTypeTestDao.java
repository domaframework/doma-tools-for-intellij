package doma.example.dao.inspection.returntype;

import org.seasar.doma.*;
import doma.example.domain.Salary;
import org.seasar.doma.jdbc.Reference;
import org.seasar.doma.jdbc.Result;

import java.util.List;
import java.util.Optional;

@Dao
public interface DataTypeReturnTypeTestDao {

  @Select
  @Sql("select * from salary where id = /*salary.value*/0")
  Salary selectSalary(Salary salary);

  @Select
  @Sql("select * from salary where id = /*salary.value*/0")
  Optional<Salary> selectOptSalary(Salary salary) ;

  @Update
  @Sql("UPDATE salary SET val = /*salary.value*/0")
  int updateSalaryWithSql1(Salary salary);

  @Update
  @Sql("UPDATE salary SET val = /*salary.value*/0")
  Result<Salary> <error descr="The return type must be \"int\"">updateSalaryWithSql2</error>(Salary salary);

  @Insert
  @Sql("INSERT INTO salary (val) VALUES (/*salary.value*/0)")
  int insertSalaryWithSql(Salary salary);

  @Insert
  @Sql("INSERT INTO salary (val) VALUES (/*salary.value*/0)")
  Result<Salary> <error descr="The return type must be \"int\"">insertSalaryWithSql2</error>(Salary salary);

  @Delete
  @Sql("DELETE FROM salary WHERE val = /*salary.value*/0 ")
  int deleteSalaryWithSql(Salary salary);

  @Delete
  @Sql("DELETE FROM salary WHERE val = /*salary.value*/0 ")
  Result<Salary> <error descr="The return type must be \"int\"">deleteSalaryWithSql2</error>(Salary salary);

  @BatchUpdate
  @Sql("UPDATE salary SET val = /*salary.value*/0")
  int[] batchUpdateSalaryWithSql(List<Salary> salary);

  @BatchUpdate
  @Sql("UPDATE salary SET val = /*salary.value*/0")
  <error descr="Cannot resolve symbol 'BatchResult'">BatchResult</error><Salary> <error descr="The return type must be \"int[]\"">batchUpdateSalaryWithSql2</error>(List<Salary> salary);

  @BatchInsert
  @Sql("INSERT INTO salary (val) VALUES (/*salary.value*/0)")
  int[] batchInsertSalaryWithSql(List<Salary> salary);

  @BatchInsert
  @Sql("INSERT INTO salary (val) VALUES (/*salary.value*/0)")
  <error descr="Cannot resolve symbol 'BatchResult'">BatchResult</error><Salary> <error descr="The return type must be \"int[]\"">batchInsertSalaryWithSql2</error>(List<Salary> salary);

  @BatchDelete
  @Sql("DELETE FROM salary WHERE val = /*salary.value*/0 ")
  int[] batchDeleteSalaryWithSql(List<Salary> salary);

  @BatchDelete
  @Sql("DELETE FROM salary WHERE val = /*salary.value*/0 ")
  <error descr="Cannot resolve symbol 'BatchResult'">BatchResult</error><Salary> <error descr="The return type must be \"int[]\"">batchDeleteSalaryWithSql2</error>(List<Salary> salary);

  @Function
  List<Salary> getTopSalaries(@In Salary limit);

  @Function
  Optional<Salary> getMaxSalary(@InOut Reference<Salary> percentage, @ResultSet List<Salary> resultSet);

  @Procedure
  void computeBonus(@In Salary employeeId);

  @Procedure
  void adjustSalaries(@InOut Reference<Salary> percentage, @ResultSet List<Salary> resultSet);

}