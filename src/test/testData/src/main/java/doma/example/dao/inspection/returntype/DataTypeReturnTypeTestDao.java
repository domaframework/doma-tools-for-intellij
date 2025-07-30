package doma.example.dao.inspection.returntype;

import org.seasar.doma.*;
import doma.example.domain.Salary;
import org.seasar.doma.jdbc.Reference;

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

  @Function
  Salary calculateAverageSalary();
  
  @Function
  Optional<Salary> getMaxSalary(@InOut Reference<Salary> percentage, @ResultSet List<Salary> resultSet);

  @Procedure
  void computeBonus(@In Salary employeeId);

  @Procedure
  void adjustSalaries(@InOut Reference<Salary> percentage, @ResultSet List<Salary> resultSet);

}