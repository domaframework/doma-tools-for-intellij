package doma.example.dao.quickfix;

import java.util.List;
import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.Sql;

@Dao
interface SelectQuickFixTestDao {

  @Select
  List<Employee> existsSQLFile(String name);

  @Select
  List<Employee> generateSQL<caret>File(String name);

  @Select
  @Sql("select * from employee where name = /* name */'test'")
  Employee nonExistSQLFile(String name);

}