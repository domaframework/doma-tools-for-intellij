package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.BatchDelete;
import org.seasar.doma.Dao;

import java.util.List;

@Dao
public interface BatchDeleteWithSqlFileDao {
    @BatchDelete(sqlFile = true)
    int[] batchDeleteEmployees(List<Employee> employees);
}