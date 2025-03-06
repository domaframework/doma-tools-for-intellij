package doma.example.dao.gutteraction;

import java.util.List;
import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.Sql;

@Dao
interface InvalidCaretTestDao {

  @Select
  Employee nonExistSQLFile(String name);
  <caret>
}