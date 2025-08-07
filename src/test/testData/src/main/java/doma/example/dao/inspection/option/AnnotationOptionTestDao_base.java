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
    @Update(include = {"name", "invalidField"})
    int updateWithInvalidInclude(Department department);

    // Valid exclude option
    @Update(exclude = {"managerCount"})
    int updateWithValidExclude(Department department);
    
    // Invalid exclude option  
    @Insert(exclude = {"salary", "location"})
    int insertWithInvalidExclude(Department department);
    
    // Mixed valid and invalid
    @Update(include = {"name"}, exclude = {"bonus"})
    int updateWithMixedOptions(Department department);
    
    // sqlFile = true should ignore include/exclude
    @Update(sqlFile = true, include = {"invalidField"})
    int updateWithSqlFile(Department department);
    
    // BatchUpdate with invalid include
    @BatchUpdate(include = {"email"})
    int batchUpdateWithInvalidInclude(List<Department> departments);
    
    // BatchUpdate with valid exclude
    @BatchInsert(exclude = {"id", "managerCount"})
    int batchInsertWithValidExclude(List<Department> departments);
    
    // Non-entity parameter - no validation
    @Update(include = {"name"})
    int updateNonEntity(String name);

    // End Embedded Property - should show error for Embeddable type needing property specification
    @Update(include = {"embeddableEntity"})
    int updateEmbedded(Department department);

    // Valid Embedded Property specification
    @Insert(include = {"embeddableEntity.age", "embeddableEntity.id"})
    int insertEmbeddedWithProperties(Department department);

    // Valid returning options
    @Update(returning = @Returning(include = {"embeddableEntity.name"}))
    Department updateReturning(Department department);

    @Insert(returning = @Returning(exclude = {"embeddableEntity.id"}))
    Department insertReturning(Department department);

    @MultiInsert(returning = @Returning(include = {"embeddableEntity.name"},exclude = {"embeddableEntity.id"}))
    List<Department> multiInsertReturning(List<Department> departments);

    // InValid returning options
    @Update(returning = @Returning(include = {"embeddableEntity.age"}))
    Department updateReturning(Department department);

    @Insert(returning = @Returning(exclude = {"embeddableEntity"}))
    Department insertReturning(Department department);

    @MultiInsert(returning = @Returning(include = {"email"},
                                        exclude = {"embeddableEntity.salary"}))
    List<Department> multiInsertReturning(List<Department> departments);
}