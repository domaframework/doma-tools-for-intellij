package doma.example.dao.gutteraction;

import java.util.List;
import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface JumpDeclarationActionSqlAnnotationTestDao {

  @Select
  @Sql("""
      select * from emp 
        where name = /* employee.employee<caret>Name */'name'
      """)
  List<Employee> jumpToEntity(Employee employee);

}