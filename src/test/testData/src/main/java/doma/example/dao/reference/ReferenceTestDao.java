package doma.example.dao.reference;

import java.util.List;
import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
public interface ReferenceTestDao {

  @Select
  Map<String, Object> referenceDaoParameter(Long reportId, String tableName, Set<String> columns);

  @Select
  Project referenceEntityProperty(List<Project> projects, ProjectDetail detail);

  @Select
  ProjectDetail referenceStaticField(ProjectDetail detail);

  @Select
  List<Employee> referenceListFieldMethod(List<Employee> employeesList);

  @Select
  List<Employee> referenceForItem(List<List<Employee>> employeesList);

  @Select
  Project referenceCustomFunction(ProjectDetail detail);

}