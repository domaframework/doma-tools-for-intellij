package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.BatchInsert;
import org.seasar.doma.Dao;
import org.seasar.doma.Sql;

import java.util.List;

@Dao
public interface BatchInsertWithSqlFileDao {
    @BatchInsert
    @Sql("""
            INSERT INTO employee
                        (id
                         , name
                         , age)
                 VALUES ( /* employees.id */1
                          , /* employees.name */'John'
                          , /* employees.age */30 )
            """)
    int[] batchInsertEmployees(List<Employee> employees);
}