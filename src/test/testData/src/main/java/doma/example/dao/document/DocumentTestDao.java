package doma.example.dao.document;

import java.util.List;
import java.util.HashSet;
import doma.example.entity.*;
import org.seasar.doma.*;
import java.util.Optional;

@Dao
public interface DocumentTestDao {

  @Select
  List<Employee> documentForItemDaoParam(HashSet<List<List<Integer>>> employeeIdsList);

  @Select
  List<Employee> documentForItemDeclaration(HashSet<List<List<Integer>>> employeeIdsList);

  @Select
  List<Employee> documentForItemElement(HashSet<List<List<Integer>>> employeeIdsList);

  @Select
  List<Employee> documentForItemElementInBindVariable(HashSet<List<List<Integer>>> employeeIdsList);

  @Select
  List<Employee> documentForItemElementInIfDirective(HashSet<List<List<Integer>>> employeeIdsList);

  @Select
  List<Employee> documentForItemElementByFieldAccess(List<List<Employee>> employeesList);

  @Select
  int documentForItemFirstElement(Principal principal);

  @Select
  int documentForItemStaticProperty();

  @Select
  int documentForItemHasNext(Principal principal);

  @Select
  Project documentForItemOptionalForItem(Optional<List<Optional<Project>>> optionalProjects);

  @Select
  Project documentForItemOptionalProperty(Optional<List<Optional<Project>>> optionalProjects);

  @Select
  int documentForItemInvalidPrimary(Principal item, Principal principal);

}