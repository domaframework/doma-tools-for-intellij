package doma.example.dao.quickfix;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Delete;

@Dao
interface DeleteQuickFixTestDao {



  @Delete
  int nonExistSQLFile(Employee employee);


  @Delete(sqlFile = true)
  int generateSQLFile<caret>(Employee employee);


  @Delete(sqlFile = true)
  int existsSQLFile(Employee employee);
}