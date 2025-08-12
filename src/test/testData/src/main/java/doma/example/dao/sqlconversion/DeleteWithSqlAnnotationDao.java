package doma.example.dao.sqlconversion;

import doma.example.User;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Update;
import org.seasar.doma.Delete;
import org.seasar.doma.Returning;
import org.seasar.doma.Sql;

@Dao
public interface DeleteWithSqlAnnotationDao {
    @Insert
    @Sql("INSERT INTO users (name, email) VALUES (/* user.name */'test', /* user.email */'test@example.com')")
    int insert(User user);

    @Delete(sqlFile = false)
    @Sql("""
			DELETE FROM employee
			 WHERE id = /* employee.employeeId */0
			""")
    int deleteEm<caret>ployeeHasSqlFile(Employee employee);

    @Insert(returning = @Returning)
    @Sql("""
        INSERT INTO employee
                    (id
                     , name)
             VALUES ( /* employee.employeeId */0
                      , /* employee.userName */'name' )
        """)
    int insertEmployeeReturning(Employee employee);
}