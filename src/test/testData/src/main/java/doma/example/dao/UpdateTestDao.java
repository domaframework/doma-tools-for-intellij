package doma.example.dao;

import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface UpdateTestDao {

  @Update
  int nonExistSQLFile(Employee employee);

  @Update(sqlFile = true)
  @Sql("update employee set id = /* employee.employeeId */0, name = /* employee.userName */'name'")
  int nonExistSQLFileAndTemplateIncluded(Employee employee);

  @Update(sqlFile = true)
  int <error descr="SQL file does not exist">nonExistSQLFileError</error>(Employee employee);

  @Update(sqlFile = true)
  int existsSQLFile(Employee employee);
}