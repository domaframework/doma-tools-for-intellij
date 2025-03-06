package doma.example.dao.gutteraction;

import java.util.List;
import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface JumpActionTestDao {

  @Select
  List<Employee> jumpToDaoFile(String name);

  @Select
  List<Employee> jumpToDaoMethodArgumentDefinition(Project project);

  @Select
  List<Employee> jumpToClassFieldDefinition(Employee employee);

  @Insert(sqlFile = true)
  int jumpsToClassMethodDefinition(Employee employee);

  @Insert(sqlFile = true)
  int jumpToStaticFieldDefinition(ProjectDetail detail);

  @Insert(sqlFile = true)
  int jumpToStaticMethodDefinition(ProjectDetail detail);

}