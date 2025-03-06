package doma.example.dao;

import java.util.List;
import doma.example.entity.*;
import org.seasar.doma.*;


@Dao
interface BatchUpdateTestDao {

  @BatchUpdate
  int[] nonExistSQLFile(List<Employee> employee);

  @BatchUpdate(sqlFile = true)
  @Sql("update employee set id = /* employees.employeeId */0, name = /* employees.userName */'name'")
  int[] nonExistSQLFileAndTemplateIncluded(List<Employee> employees);

  @BatchUpdate(sqlFile = true)
  int[] <error descr="SQL file does not exist">nonExistSQLFileError</error>(List<Employee> employees);

  @BatchUpdate(sqlFile = true)
  int[] existsSQLFile(List<Employee> employees);
}