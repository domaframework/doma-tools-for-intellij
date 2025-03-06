package doma.example.dao.gutteraction;

import java.util.List;
import doma.example.entity.Employee;
import org.seasar.doma.BatchUpdate;
import org.seasar.doma.Dao;

@Dao
interface BatchUpdateInvalidCaretTestDao {

  
  @BatchUpdate(sqlFile = true)
  @Sql("update employee set name = /* employees.name */'test' where id = /* employees.id */1")
  int[] NoSqlFile<caret>WithTemplate(List<Employee> employees);

  @BatchUpdate(sqlFile = true)
  int[] existsSQLFile1(List<Employee> employees);

  @BatchUpdate(sqlFile = true)
  int[] existsSQLFile2(List<Employee> employees);

}