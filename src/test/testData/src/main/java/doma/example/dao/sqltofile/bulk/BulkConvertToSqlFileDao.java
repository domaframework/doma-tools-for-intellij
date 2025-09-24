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
public interface BulkConvertToSqlFileDao<caret> {

    @Select
    @Sql("SELECT * FROM emp WHERE id = /* employee.getSubEmployee(project).projectId */0")
    String generateSqlFile(Employee employee, Project project);

    /**
     * Confirm that formatting is performed after the action
     *
     * @param id
     * @return
     */
    @Select
    @Sql("""
          SELECT *
            FROM employee WHERE id = /* id */1
          """)
    Employee selectEmployee(int id);

    @Insert
    @Sql("""
          INSERT INTO employee(id, name)
               VALUES ( /* employee.employeeId */0, /* employee.userName */'name' )""")
    int insertEmployee(Employee employee);

    @Update
    @Sql("""
          UPDATE employee
             SET name = /* employee.employeeName */'John', rank = /* employee.rank */30
           WHERE id = /* employee.managerId */1
          """)
    int updateEmployee(Employee employee);

    @Delete
    @Sql("""
          DELETE 
            FROM employee
           WHERE id = /* employee.managerId */1
          """)
    int deleteEmployee(Employee employee);

    @BatchInsert
    @Sql("""
          INSERT INTO employee
                      (id, name, age)
               VALUES ( /* employees.employeeId */1, /* employees.employeeName */'John', /* employees.rank */30 )
          """)
    int[] batchInsertEmployees(List<Employee> employees);

    @BatchUpdate
    @Sql("""
          UPDATE employee
             SET name = /* employees.employeeName */'John', rank = /* employees.rank */30
           WHERE id = /* employees.employeeId */1
          """)
    int[] batchUpdateEmployees(List<Employee> employees);

    @BatchDelete
    @Sql("""
          DELETE FROM employee WHERE id = /* employees.employeeId */1
          """)
    int[] batchDeleteEmployees(List<Employee> employees);

    @Script
    @Sql("""
          CREATE TABLE employee
            ( id INTEGER PRIMARY KEY, name VARCHAR(100),  age INTEGER) ;
          CREATE INDEX idx_employee_name ON employee(name) ;
          """)
    void createTables();

    @SqlProcessor
    @Sql("""
          SELECT count(*) FROM employee WHERE age > 25
          """)
    <R> R processData(BiFunction<Config, PreparedSql, R> processor);

    // Annotation not supported
    @Function
    void executeFunc();
}
