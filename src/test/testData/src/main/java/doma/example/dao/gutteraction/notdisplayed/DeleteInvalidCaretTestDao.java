package doma.example.dao.gutteraction;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Delete;

@Dao
interface DeleteInvalidCaretTestDao {

  
  @Delete
  int nonRequireSQL<caret>File(Employee employee);

  @Delete(sqlFile = true)
  int nonExistSQLFileError(Employee employee);

  @Delete(sqlFile = true)
  int existsSQLFile1(Employee employee);
}