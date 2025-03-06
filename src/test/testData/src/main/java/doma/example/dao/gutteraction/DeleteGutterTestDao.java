package doma.example.dao.gutteraction;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Delete;

@Dao
interface DeleteGutterTestDao {

  @Delete
  int nonExistSQLFile(Employee employee);

  @Delete(sqlFile = true)
  int nonExistSQLFileError(Employee employee);

  @Delete(sqlFile = true)
  int existsSQL<caret>File1(Employee employee);
}