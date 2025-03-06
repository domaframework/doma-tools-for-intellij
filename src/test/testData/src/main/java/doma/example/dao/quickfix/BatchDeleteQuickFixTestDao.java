package doma.example.dao.quickfix;

import java.util.List;
import doma.example.entity.Employee;
import org.seasar.doma.BatchDelete;
import org.seasar.doma.Dao;

@Dao
public interface BatchDeleteQuickFixTestDao {



  @BatchDelete
  int[] nonExistSQLFile(List<Employee> employees);


  @BatchDelete(sqlFile = true)
  int[] generateSQLFile<caret>(List<Employee> employees);


  @BatchDelete(sqlFile = true)
  int[] existsSQLFile(List<Employee> employees);

}