package doma.example.dao.quickfix;

import java.util.List;
import doma.example.entity.Employee;
import org.seasar.doma.BatchUpdate;
import org.seasar.doma.Dao;

@Dao
interface BatchUpdateQuickFixTestDao {


  @BatchUpdate
  int[] nonExistSQLFile(List<Employee> employee);


  @BatchUpdate(sqlFile = true)
  int[] generateSQLFile<caret>(List<Employee> employees);


  @BatchUpdate(sqlFile = true)
  int[] existsSQLFile(List<Employee> employees);
}