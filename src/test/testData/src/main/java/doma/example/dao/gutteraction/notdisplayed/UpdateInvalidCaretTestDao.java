package doma.example.dao.gutteraction;

import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface UpdateInvalidCaretTestDao {
 
  @Update(sqlFile = true)
  int existsSQLFile1(Employee employee);

  @Update(sqlFile = true)
  @Sql("update employee set name = /* employee.name */'hoge' where id = /* employee.id */1")
  int nonRequireSQL<caret>File(Employee employee);

  @Update(sqlFile = true)
  int nonExistSQLFileError(Employee employee);

  @Update(sqlFile = true)
  int existsSQLFile2(Employee employee);
}