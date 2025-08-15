package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.BatchDelete;
import org.seasar.doma.Dao;
import org.seasar.doma.Sql;

import java.util.List;

@Dao
public interface BatchDeleteWithSqlFileDao {
    @BatchDelete
    @Sql("""
            DELETE FROM employee
             WHERE id = /* employees.id */1
            """)
    int[] batchDeleteEmployees(List<Employee> employees);
}