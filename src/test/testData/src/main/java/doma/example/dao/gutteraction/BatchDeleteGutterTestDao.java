package doma.example.dao.gutteraction;

import java.util.List;
import doma.example.entity.Employee;
import org.seasar.doma.BatchDelete;
import org.seasar.doma.Dao;

@Dao
public interface BatchDeleteGutterTestDao {

  @BatchDelete(sqlFile = true)
  int[] existsSQLFile1(List<Employee> employees);

  @BatchDelete(sqlFile = true)
  int[] existsSQL<caret>File2(List<Employee> employees);
}