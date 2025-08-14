package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;

import java.util.List;

@Dao
public interface SelectWithComplexSqlFileDao {
    @Select
    List<Employee> selectComplexQuery(String department, int minAge, int maxAge);
}