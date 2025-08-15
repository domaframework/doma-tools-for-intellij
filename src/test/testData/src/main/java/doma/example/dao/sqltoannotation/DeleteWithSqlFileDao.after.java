package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Delete;
import org.seasar.doma.Sql;

@Dao
public interface DeleteWithSqlFileDao {
    @Delete
    @Sql("""
            DELETE FROM employee
             WHERE id = /* employee.id */1
            """)
    int deleteEmployee(Employee employee);
}