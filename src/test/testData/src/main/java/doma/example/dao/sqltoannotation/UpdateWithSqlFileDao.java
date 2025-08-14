package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Update;

@Dao
public interface UpdateWithSqlFileDao {
    @Update(sqlFile = true)
    int updateEmployee(Employee employee);
}