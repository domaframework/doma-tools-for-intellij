package doma.example.dao;

import java.util.List;
import doma.example.entity.*;
import org.seasar.doma.*;


@Dao
interface SelectTestDao {
  
  @Select
  List<Employee> existsSQLFile(String name);
  
  @Select
  List<Employee> <error descr="SQL file does not exist">nonExistSQLFileError</error>(String name);
  
  @Select
  @Sql("select * from employee where name = /* name */'test'")
  Employee nonExistSQLFile(String name);

}