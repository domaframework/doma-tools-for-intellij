package doma.example.dao.gutteraction;

import java.util.List;
import doma.example.entity.Employee;
import org.seasar.doma.BatchDelete;
import org.seasar.doma.Dao;

@Dao
public interface BatchDeleteInvalidCaretTestDao {

  @BatchDelete
  int[] nonRequireSQL<caret>File(List<Employee> employees);

}