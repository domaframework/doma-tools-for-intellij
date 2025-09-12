package doma.example.dao.inspection.option;

import doma.example.entity.Department;
import doma.example.entity.NonSubEntity;
import doma.example.entity.Pckt;
import doma.example.entity.SubEntity;
import org.seasar.doma.*;
import org.seasar.doma.jdbc.MultiResult;

import java.util.List;

/**
 * When using this test data with [LightJavaCodeInsightFixtureTestCase],
 * the highlighted elements are not parsed correctly, resulting in errors.
 * Compare the highlights in this test data with the actual highlights in IDEA,
 * and visually confirm that the highlights and errors occur as expected based on the test data.
 */
@Dao
public interface AnnotationOptionTestInValidDao {

    /**
     * include: Error highlight when specifying fields not defined in the parameter type
     * Error: Field [invalidField] specified in [include] option does not exist in "Department". Available fields: [id, name, location, managerCount, subId, embeddableEntity, embeddableEntity2]
     * @param department
     * @return
     */
    @Update(include = {"name", "invalidField"})
    int updateWithInvalidInclude(Department department);

    /**
     * exclude: Error highlight when specifying fields not defined in the parameter type
     * Error: Field [salary] specified in [exclude] option does not exist in "Department". Available fields: [id, name, location, managerCount, subId, embeddableEntity, embeddableEntity2]
     * @param department
     * @return
     */
    @Insert(exclude = {"salary", "location"})
    int insertWithInvalidExclude(Department department);

    /**
     * MultiInsert: Error highlight when specifying fields not defined in immutable Entity
     * Also error highlight when specifying fields not defined in the parameter type
     * Error: Field [salary] specified in [include] option does not exist in "Department". Available fields: [id, name, location, managerCount, subId, embeddableEntity, embeddableEntity2]
     *        Field [bonus] specified in [exclude] option does not exist in "Department". Available fields: [id, name, location, managerCount, subId, embeddableEntity, embeddableEntity2]
     * @param departments
     * @return
     */
    @MultiInsert(include = {"salary"}, exclude = {"bonus"})
    int multiInsert(List<Department> departments);

    /**
     * MultiInsert: Error highlight when specifying fields not defined in mutable Entity in include/exclude
     * Error: Field [salary] specified in [include] option does not exist in "Pckt". Available fields: [id, name]
     *        Field [bonus] specified in [exclude] option does not exist in "Pckt". Available fields: [id, name]
     * @param pckts
     * @return
     */
    @MultiInsert(include = {"salary"}, exclude = {"bonus"})
    MultiResult<Pckt> multiInsertImpairmentEntity(List<Pckt> pckts);

    /**
     * Batch annotations: Error highlight when specifying fields not defined in the parameter type
     * Error: Field [email] specified in [include] option does not exist in "Department". Available fields: [id, name, location, managerCount, subId, embeddableEntity, embeddableEntity2]
     * @param departments
     * @return
     */
    @BatchUpdate(include = {"email"})
    int[] batchUpdateWithInvalidInclude(List<Department> departments);

    /**
     * Error when ending with an Embedded property
     * Error: Field [embeddableEntity] specified in [include] option is an Embeddable type "ClientUser". Must specify its properties. Available properties: [id, name, number]
     */
    @Update(include = {"embeddableEntity"})
    int updateEmbedded(Department department);

    /**
     * Error when there is a mistake in Embedded class properties
     * Error: Field [age] specified in [include] option does not exist in "ClientUser". Available fields: [id, name, number, childEmbedded, childEmbedded2]
     */
    @Insert(include = {"embeddableEntity.age", "embeddableEntity.id"})
    int insertEmbeddedWithProperties(Department department);

    /**
     * Same check applies when using Returning
     * Error: Field [age] specified in [include] option does not exist in "ClientUser". Available fields: [id, name, number, childEmbedded, childEmbedded2]
     */
    @Update(returning = @Returning(include = {"embeddableEntity.age"}))
    Department updateReturning(Department department);

    /**
     * Error highlight when specifying invalid fields in Returning option
     * Error: Field [embeddableEntity] specified in [exclude] option is an Embeddable type "ClientUser". Must specify its properties. Available properties: [id, name, number]
     * @param department
     * @return
     */
    @Insert(returning = @Returning(exclude = {"embeddableEntity"}))
    Department insertReturning(Department department);

    /**
     * MultiInsert Returning option: Error highlight in both include/exclude when specifying invalid fields
     * Error: Field [email] specified in [include] option does not exist in "Department". Available fields: [id, name, location, managerCount, subId, embeddableEntity, embeddableEntity2]
     * @param departments
     * @return
     */
    @MultiInsert(returning = @Returning(include = {"email"}, exclude = {"embeddableEntity.salary"}))
    List<Department> multiInsertReturning(List<Department> departments);

    /**
     * Error highlight when specifying fields not defined in mutable Entity within Returning option
     * Error: Field [embeddableEntity] specified in [include] option does not exist in "Pckt". Available fields: [id, name]
     */
    @Update(returning = @Returning(include = {"embeddableEntity.age"}))
    Pckt updateReturning(Pckt pckt);

    /**
     * Error highlight when specifying fields not defined in Embedded property
     * Error: Field [age] specified in [include] option does not exist in "ClientUser". Available fields: [id, name, number, childEmbedded, childEmbedded2]
     */
    @Update(returning = @Returning(include = "embeddableEntity.age"))
    Department updateSingleInclude(Department department);

    /**
     * Error highlight when specifying further properties from a Primitive type
     * Error: Field path [subId.get] specified in [exclude] option is invalid. Field [get] is a primitive type and does not have nested properties
     */
    @Insert(exclude = "subId.get")
    int insertPrimitiveProperty(Department department);
}