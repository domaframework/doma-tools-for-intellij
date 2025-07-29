package doma.example.dao.inspection.option;

import doma.example.entity.Department;
import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Update;
import org.seasar.doma.BatchUpdate;
import java.util.List;

@Dao
public interface AnnotationOptionTestDao {
    
    // Valid include option
    @Update(include = {"name", "location"})
    int updateWithValidInclude(Department department);
    
    // Invalid include option
    @Update(include = {"name", <error descr="Field 'invalidField' specified in 'include' option does not exist in the entity. Available fields: id, location, managerCount, name">"invalidField"</error>})
    int updateWithInvalidInclude(Department department);
    
    // Valid exclude option
    @Update(exclude = {"managerCount"})
    int updateWithValidExclude(Department department);
    
    // Invalid exclude option  
    @Update(exclude = {<error descr="Field 'salary' specified in 'exclude' option does not exist in the entity. Available fields: id, location, managerCount, name">"salary"</error>, "location"})
    int updateWithInvalidExclude(Department department);
    
    // Mixed valid and invalid
    @Update(include = {"name"}, exclude = {<error descr="Field 'bonus' specified in 'exclude' option does not exist in the entity. Available fields: id, location, managerCount, name">"bonus"</error>})
    int updateWithMixedOptions(Department department);
    
    // sqlFile = true should ignore include/exclude
    @Update(sqlFile = true, include = {"invalidField"})
    int updateWithSqlFile(Department department);
    
    // BatchUpdate with invalid include
    @BatchUpdate(include = {<error descr="Field 'email' specified in 'include' option does not exist in the entity. Available fields: id, location, managerCount, name">"email"</error>})
    int batchUpdateWithInvalidInclude(List<Department> departments);
    
    // BatchUpdate with valid exclude
    @BatchUpdate(exclude = {"id", "managerCount"})
    int batchUpdateWithValidExclude(List<Department> departments);
    
    // Non-entity parameter - no validation
    @Update(include = {"name"})
    int updateNonEntity(String name);
    
    // Employee entity
    @Update(include = {<error descr="Field 'address' specified in 'include' option does not exist in the entity. Available fields: employeeId, employeeName, project, userName">"address"</error>})
    int updateEmployee(Employee employee);
}