package doma.example.dao.gutteraction;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Update;

@Dao
interface UpdateGutterTestDao {

  @Update
  int nonExistSQLFile(Employee employee);

  @Update(sqlFile = true)
  int existsSQL<caret>File1(Employee employee);

  @Update
  int nonExistSQLFile(Employee employee);

  @Update(sqlFile = true)
  int <error descr="SQL file does not exist">nonExistSQLFileError</error>(Employee employee);

  @Update(sqlFile = true)
  int existsSQLFile2(Employee employee);
}