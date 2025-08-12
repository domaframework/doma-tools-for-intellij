package doma.example.dao.sqlconversion;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.Sql;

@Dao
public interface SelectOverrideSqlFileDao {
  @Select
  Employee overrideSqlFile(Employee employee);
}