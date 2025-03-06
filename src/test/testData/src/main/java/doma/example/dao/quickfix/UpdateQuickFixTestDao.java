package doma.example.dao.quickfix;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Update;

@Dao
interface UpdateQuickFixTestDao {

  @Update
  int nonExistSQLFile(Employee employee);

  @Update(sqlFile = true)
  int generateSQLFile<caret>(Employee employee);

  @Update(sqlFile = true)
  int existsSQLFile(Employee employee);
}