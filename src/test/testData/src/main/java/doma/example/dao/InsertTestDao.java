package doma.example.dao;

import doma.example.entity.*;
import org.seasar.doma.*;


@Dao
public interface InsertTestDao {

  @Insert
  int nonExistSQLFile(Employee employee);

  @Insert
  @Sql("insert into employee (id, name) values (/* employee.employeeId */0, /* employee.name */'name')")
  int nonExistSQLFileAndTemplateIncluded(Employee employee);

 @Insert(sqlFile = true)
  int <error descr="SQL file does not exist">nonExistSQLFileError</error>(Employee employee, String orderBy);

  @Insert(sqlFile = true)
  int existsSQLFile(Employee employee, String orderBy);
}