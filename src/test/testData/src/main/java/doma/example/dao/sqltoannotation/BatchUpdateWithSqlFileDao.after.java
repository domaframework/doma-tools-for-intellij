package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.BatchUpdate;
import org.seasar.doma.Dao;
import org.seasar.doma.Sql;

import java.util.List;

@Dao
public interface BatchUpdateWithSqlFileDao {
    @BatchUpdate
    @Sql("""
            UPDATE employee
               SET name = /* employees.name */'John'
                   , age = /* employees.age */30
             WHERE id = /* employees.id */1
            """)
    int[] batchUpdateEmployees(List<Employee> employees);
}