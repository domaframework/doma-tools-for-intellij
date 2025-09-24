package doma.example.dao.sqltofile.bulk;

import doma.example.entity.Employee;
import doma.example.entity.Project;
import java.util.List;
import java.util.function.BiFunction;
import org.seasar.doma.BatchDelete;
import org.seasar.doma.BatchInsert;
import org.seasar.doma.BatchUpdate;
import org.seasar.doma.Dao;
import org.seasar.doma.Delete;
import org.seasar.doma.Function;
import org.seasar.doma.Insert;
import org.seasar.doma.Script;
import org.seasar.doma.Select;
import org.seasar.doma.Sql;
import org.seasar.doma.SqlProcessor;
import org.seasar.doma.Update;
import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;

/**
 * Bulk annotation type conversion test (Sql annotation ⇒ SQL file)
 */
@Dao
public interface BulkConvertToSqlFileDao {

    @Select
    String generateSqlFile(Employee employee, Project project);

    /**
     * Confirm that formatting is performed after the action
     *
     * @param id
     * @return
     */
    @Select
    Employee selectEmployee(int id);

    @Insert
    int insertEmployee(Employee employee);

    @Update
    int updateEmployee(Employee employee);

    @Delete
    int deleteEmployee(Employee employee);

    @BatchInsert
    int[] batchInsertEmployees(List<Employee> employees);

    @BatchUpdate
    int[] batchUpdateEmployees(List<Employee> employees);

    @BatchDelete
    int[] batchDeleteEmployees(List<Employee> employees);

    @Script
    void createTables();

    @SqlProcessor
    <R> R processData(BiFunction<Config, PreparedSql, R> processor);

    // Annotation not supported
    @Function
    void executeFunc();
}