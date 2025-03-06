package doma.example.dao;

import java.util.List;
import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
public interface BatchDeleteTestDao {

  @BatchDelete
  int[] nonExistSQLFile(List<Employee> employees);

  @BatchDelete(sqlFile = true)
  @Sql("delete from employee where id = /* employees.employeeId */0 and name = /* employees.userName */'name'")
  int[] nonExistSQLFileAndTemplateIncluded(List<Employee> employees);

  @BatchDelete(sqlFile = true)
  int[] <error descr="SQL file does not exist">nonExistSQLFileError</error>(List<Employee> employees);

  @BatchDelete(sqlFile = true)
  int[] existsSQLFile(List<Employee> employees);

}