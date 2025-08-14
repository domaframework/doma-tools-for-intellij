package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.BatchUpdate;
import org.seasar.doma.Dao;

import java.util.List;

@Dao
public interface BatchUpdateWithSqlFileDao {
    @BatchUpdate(sqlFile = true)
    int[] batchUpdateEmployees(List<Employee> employees);
}