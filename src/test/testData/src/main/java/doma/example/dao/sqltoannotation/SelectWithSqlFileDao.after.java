package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.Sql;

@Dao
public interface SelectWithSqlFileDao {
    @Select
    @Sql("""
            SELECT *
              FROM employee
             WHERE id = /* id */1
            """)
    Employee selectEmployee(int id);
}