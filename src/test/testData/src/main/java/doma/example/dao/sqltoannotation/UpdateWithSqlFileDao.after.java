package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Sql;
import org.seasar.doma.Update;

@Dao
public interface UpdateWithSqlFileDao {
    @Update
    @Sql("""
            UPDATE employee
               SET name = /* employee.name */'John'
                   , age = /* employee.age */30
             WHERE id = /* employee.id */1
            """)
    int updateEmployee(Employee employee);
}