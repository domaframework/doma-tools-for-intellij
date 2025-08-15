package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Delete;

@Dao
public interface DeleteWithSqlFileDao {
    @Delete(sqlFile = true)
    int deleteEmployee(Employee employee);
}