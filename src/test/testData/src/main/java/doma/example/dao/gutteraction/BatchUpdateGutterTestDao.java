package doma.example.dao.gutteraction;

import java.util.List;
import doma.example.entity.Employee;
import org.seasar.doma.BatchUpdate;
import org.seasar.doma.Dao;

@Dao
interface BatchUpdateGutterTestDao {

  @BatchUpdate
  int[] nonExistSQLFile(List<Employee> employee);

  @BatchUpdate(sqlFile = true)
  int[] existsSQLFile1(List<Employee> employees);

  @BatchUpdate(sqlFile = true)
  int[] nonExistSQLFileError(List<Employee> employees);

  @BatchUpdate(sqlFile = true)
  int[] existsSQL<caret>File2(List<Employee> employees);

  @BatchUpdate(sqlFile = true)
  int[] existsSQLFile3(List<Employee> employees);
}