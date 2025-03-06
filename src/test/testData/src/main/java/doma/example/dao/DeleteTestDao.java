package doma.example.dao;

import doma.example.entity.*;
import org.seasar.doma.*;


@Dao
interface DeleteTestDao {

  @Delete
  int nonExistSQLFile(Employee employee);

  @Delete(sqlFile = true)
  @Sql("delete from employee where id = /* employee.employeeId */0 and name = /* employee.userName */'name'")
  int nonExistSQLFileAndTemplateIncluded(Employee employee);

  @Delete(sqlFile = true)
  int <error descr="SQL file does not exist">nonExistSQLFileError</error>(Employee employee);

  @Delete(sqlFile = true)
  int existsSQLFile(Employee employee);
}