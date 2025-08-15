package doma.example.dao.sqltofile;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.Sql;

@Dao
public interface SelectOverrideSqlFileDao {
  @Select
  @Sql("select * From emp where id = /* employee.employeeId */0")
  Employee override<caret>SqlFile(Employee employee);
}