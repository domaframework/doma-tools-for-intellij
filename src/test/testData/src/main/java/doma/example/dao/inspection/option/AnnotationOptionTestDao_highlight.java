package doma.example.dao.inspection.option;

import doma.example.entity.*;
import org.seasar.doma.*;
import java.util.List;

/**
 * When using this test data with [LightJavaCodeInsightFixtureTestCase],
 * the highlighted elements are not parsed correctly, resulting in errors.
 * Compare the highlights in this test data with the actual highlights in IDEA,
 * and visually confirm that the highlights and errors occur as expected based on the test data.
 */
@Dao
public interface AnnotationOptionTestDao {
    
    // Valid include option
    @Insert(include = {"name", "location"})
    int insertWithValidInclude(Department department);

    // Invalid include option
    @Update(include = {"name", <error descr="Field [invalidField] specified in [include] option does not exist in \"Department\". Available fields: [id, name, location, managerCount, embeddableEntity, embeddableEntity2] ">"invalidField"</error>})
    int updateWithInvalidInclude(Department department);

    // Valid exclude option
    @Update(exclude = {"managerCount"})
    int updateWithValidExclude(Department department);
    
    // Invalid exclude option  
    @Insert(exclude = {<error descr="Field [salary] specified in [exclude] option does not exist in \"Department\". Available fields: [id, name, location, managerCount, embeddableEntity, embeddableEntity2] ">"salary"</error>, "location"})
    int insertWithInvalidExclude(Department department);
    
    // Mixed valid and invalid
    @Update(include = {"name"}, exclude = {<error descr="Field [bonus] specified in [exclude] option does not exist in \"Department\". Available fields: [id, name, location, managerCount, embeddableEntity, embeddableEntity2] ">"bonus"</error>})
    int updateWithMixedOptions(Department department);
    
    // sqlFile = true should ignore include/exclude
    @Update(sqlFile = true, include = {"invalidField"})
    int updateWithSqlFile(Department department);
    
    // BatchUpdate with invalid include
    @BatchUpdate(include = {<error descr="Field [email] specified in [include] option does not exist in \"Department\". Available fields: [id, name, location, managerCount, embeddableEntity, embeddableEntity2]">"email"</error>})
    int[] batchUpdateWithInvalidInclude(List<Department> departments);
    
    // BatchUpdate with valid exclude
    @BatchInsert(exclude = {"id", "managerCount"})
    int[] batchInsertWithValidExclude(List<Department> departments);
    
    // Non-entity parameter - no validation
    @Update(include = {"name"})
    int updateNonEntity(String name);

    // End Embedded Property - should show error for Embeddable type needing property specification
    @Update(include = {<error descr="Field [embeddableEntity specified in [include] option is an Embeddable type \"ClientUser\". Must specify its properties. Available properties: [id, name, number]">"embeddableEntity"</error>})
    int updateEmbedded(Department department);
    
    // Valid Embedded Property specification
    @Insert(include = {<error descr="Field [age] specified in [include] option does not exist in \"EmbeddableEntity\". Available fields: [id, name, number, childEmbedded, childEmbedded2]">"embeddableEntity.age"</error>, "embeddableEntity.id"})
    int insertEmbeddedWithProperties(Department department);

    // Valid returning options
    @Update(returning = @Returning(include = {"embeddableEntity.name"}))
    Department updateReturning(Department department);

    @Insert(returning = @Returning(exclude = {"embeddableEntity.id"}))
    Department insertReturning(Department department);

    @MultiInsert(returning = @Returning(include = {"embeddableEntity.name"},exclude = {"embeddableEntity.id"}))
    List<Department> multiInsertReturning(List<Department> departments);

    // InValid returning options
    @Update(returning = @Returning(include = {<error descr="Field [age] specified in [include] option does not exist in \"ClientUser\". Available fields: [id, name, location, managerCount, embeddableEntity, embeddableEntity2] ">"embeddableEntity.age"</error>}))
    Department updateReturning(Department department);

    @Insert(returning = @Returning(exclude = {<error descr="Field [embeddableEntity] specified in [exclude] option is an Embeddable type \"ClientUser\". Must specify its properties. Available properties: [id, name, number]">"embeddableEntity"</error>}))
    Department insertReturning(Department department);

    @MultiInsert(returning = @Returning(include = {<error descr="Field [email] specified in [include] option does not exist in \"Department\". Available fields: [id, name, location, managerCount, embeddableEntity, embeddableEntity2] ">"email"},
                                        exclude = {<error descr="Field [salary] specified in [exclude] option does not exist in \"ClientUser\". Available fields: [id, name, location, managerCount, embeddableEntity, embeddableEntity2] ">"embeddableEntity.salary"</error>}))
    List<Department> multiInsertReturning(List<Department> departments);
}