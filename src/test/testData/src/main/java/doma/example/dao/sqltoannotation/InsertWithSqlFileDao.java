package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;

@Dao
public interface InsertWithSqlFileDao {
    @Insert(sqlFile = true)
    int insertEmployee(Employee employee);
}