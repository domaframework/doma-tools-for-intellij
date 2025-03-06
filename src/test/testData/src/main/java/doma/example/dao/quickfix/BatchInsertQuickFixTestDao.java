package doma.example.dao.quickfix;

import java.util.List;
import doma.example.entity.Employee;
import org.seasar.doma.BatchInsert;
import org.seasar.doma.Dao;

@Dao
interface BatchInsertQuickFixTestDao {


  @BatchInsert
  int[] nonExistSQLFile(List<Employee> employees);


  @BatchInsert(sqlFile = true)
  int[] generateSQLFile<caret>(List<Employee> employees);


  @BatchInsert(sqlFile = true)
  int[] existsSQLFile(List<Employee> employees);
}