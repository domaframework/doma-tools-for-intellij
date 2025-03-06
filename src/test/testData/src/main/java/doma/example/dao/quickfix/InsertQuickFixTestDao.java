package doma.example.dao.quickfix;

import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface InsertQuickFixTestDao {

  @Insert
  int nonExistSQLFile(Employee employee);
  
  @Insert
  @Sql("insert into employee (id, name) values (/* employee.detail.detail.detaiDemolname */0, /* employee.name */'')")
  int nonExistSQLFileAndTemplateIncluded(Employee employee);

  @Insert(sqlFile = true)
  int generateSQLFile<caret>(Employee employee, String orderBy);
  
  @Insert(sqlFile = true)
  int existsSQLFile(Employee employee, String orderBy);
}