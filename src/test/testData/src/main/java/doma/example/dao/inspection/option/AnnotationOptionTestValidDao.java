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
public interface AnnotationOptionTestValidDao {

  // include normal case
  @Insert(include = {"name", "location"})
  int insertWithValidInclude(Department department);

  // exclude normal case
  @Update(exclude = {"managerCount"})
  int updateWithValidExclude(Department department);

  @MultiInsert(
      include = {"name"},
      exclude = {"location"})
  int multiInsert(List<Department> departments);

    // sqlFile = true should ignore include/exclude
    @Update(sqlFile = true, include = {"invalidField"})
    int updateWithSqlFile(Department department);

  // With @Sql annotation, include/exclude checks are ignored
  @Update(include = {"invalidField"})
  @Sql(
      """
			UPDATE department
			   SET
			 WHERE id = /* department.id */0
			""")
  int updateWithSqlAnnotation(Department department);

  // Batch annotation include/exclude checks
  @BatchInsert(exclude = {"id", "managerCount"})
  int[] batchInsertWithValidExclude(List<Department> departments);

    // Non-entity parameter - no validation
    @Update(include = {"name"})
    int updateNonEntity(String name);

  // Specifying properties from Embedded property
  @Insert(include = {"embeddableEntity.name", "embeddableEntity.id"})
  int insertEmbeddedWithProperties(Department department);

  // Same check applies when using Returning
  @Update(returning = @Returning(include = {"embeddableEntity.name"}))
  Department updateReturning(Department department);

  @Insert(returning = @Returning(exclude = {"embeddableEntity.id"}))
  Department insertReturning(Department department);

  @MultiInsert(
      returning =
          @Returning(
              include = {"embeddableEntity.name"},
              exclude = {"embeddableEntity.id"}))
  List<Department> multiInsertReturning(List<Department> departments);

  // Non-array property specification
  @Insert(returning = @Returning(exclude = "embeddableEntity.id"))
  Department insertSingleExclude(Department department);

  // Using Primitive type
  @Update(include = "subId")
  int updatePrimitiveProperty(Department department);

    /**
     * Case of specifying properties defined in parent class (subclass with @Entity)
     * @param subEntity
     * @return
     */
    @Insert(exclude = "name")
    int insertReferenceParentEntityProperty1(SubEntity subEntity);

    /**
     * Case of specifying properties defined in parent class (subclass without @Entity)
     * @param subEntity
     * @return
     */
    @Insert(exclude = "name")
    int insertReferenceParentEntityProperty2(NonSubEntity subEntity);
}
