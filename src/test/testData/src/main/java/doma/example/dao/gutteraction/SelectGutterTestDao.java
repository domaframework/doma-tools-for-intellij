package doma.example.dao.gutteraction;

import java.util.List;
import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.Sql;

@Dao
interface SelectGutterTestDao {

  @Select
  List<Employee> existsSQLFile<caret>1(String name);

  @Select
  List<Employee> existsSQLFile2(int id,Integer subId);

  @Select
  List<Employee> nonExistSQLFileError(String name);

  @Select
  @Sql("select * from employee where name = /* name */'test'")
  Employee nonExistSQLFile(String name);

}