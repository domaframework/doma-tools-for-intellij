package doma.example.dao.sqltoannotation;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.Sql;

import java.util.List;

@Dao
public interface SelectWithComplexSqlFileDao {
    @Select
    @Sql("""
            SELECT e.id
                   , e.name
                   , e.age
                   , d.department_name
                   , p.project_name
              FROM employee e
                   JOIN department d
                     ON e.department_id = d.id
                   LEFT JOIN employee_project ep
                          ON e.id = ep.employee_id
                   LEFT JOIN project p
                          ON ep.project_id = p.id
             WHERE d.department_name = /* department */'Sales'
               AND e.age BETWEEN /* minAge */25 AND /* maxAge */35
             ORDER BY e.name
                      , p.project_name
            """)
    List<Employee> selectComplexQuery(String department, int minAge, int maxAge);
}