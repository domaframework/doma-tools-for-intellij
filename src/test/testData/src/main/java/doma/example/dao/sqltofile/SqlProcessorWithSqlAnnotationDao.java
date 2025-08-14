package doma.example.dao.sqltofile;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.SqlProcessor;
import org.seasar.doma.Sql;
import java.util.Map;

@Dao
public interface SqlProcessorWithSqlAnnotationDao {
    @SqlProcessor
    @Sql("""
			SELECT *
			  FROM processor WHERE id = /* employee.managerId */0
			""")
    Employee execute<caret>Processor(Employee employee,BiFunction<Config, PreparedSql, Employee> biFunc);

}