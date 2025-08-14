package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.BatchInsert;
import org.seasar.doma.Dao;

import java.util.List;

@Dao
public interface BatchInsertWithSqlFileDao {
    @BatchInsert(sqlFile = true)
    int[] batchInsertEmployees(List<Employee> employees);
}