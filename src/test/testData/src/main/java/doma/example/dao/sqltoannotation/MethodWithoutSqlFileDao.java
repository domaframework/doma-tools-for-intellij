package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;

@Dao
public interface MethodWithoutSqlFileDao {
    @Select
    Employee selectEmployee(int id);
}