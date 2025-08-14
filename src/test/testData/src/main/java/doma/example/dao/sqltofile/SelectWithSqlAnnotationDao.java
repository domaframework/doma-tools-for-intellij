package doma.example.dao.sqltofile;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.Sql;

@Dao
public interface SelectWithSqlAnnotationDao {
    @Select
    @Sql("select * From emp where id = /* employee.employeeId */0")
    Employee generate<caret>SqlFile(Employee employee);

    @Select
    @Sql("""
			SELECT * FROM emp
			 WHERE id = /* employee.employeeId */0
			""")
    Employee generateSqlFileByTextBlock(Employee employee);

    @Select(strategy = SelectType.COLLECT)
    @Sql("""
          SELECT *
            FROM emp
           WHERE id = /* employee.employeeId */0
          """)
    Employee generateSqlFileHasAnyOption(Employee employee, Collector<Integer, BigDecimal, Employee> collector);

}