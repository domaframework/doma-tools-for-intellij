package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Sql;

@Dao
public interface InsertWithSqlFileDao {
    @Insert
    @Sql("""
            INSERT INTO employee
                        (id
                         , name
                         , age)
                 VALUES ( /* employee.id */1
                          , /* employee.name */'John'
                          , /* employee.age */30 )
            """)
    int insertEmployee(Employee employee);
}