package doma.example.dao.sqlconversion;

import doma.example.User;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Update;
import org.seasar.doma.Delete;
import org.seasar.doma.Returning;
import org.seasar.doma.Sql;

@Dao
public interface InsertWithSqlAnnotationDao {
    @Insert
    @Sql("INSERT INTO users (name, email) VALUES (/* user.name */'test', /* user.email */'test@example.com')")
    int ins<caret>ert(User user);

    @Delete(sqlFile = false)
    @Sql("""
			DELETE FROM employee
			 WHERE id = /* employee.employeeId */0
			""")
    int deleteEmployeeHasSqlFile(Employee employee);

    @Update(returning = @Returning)
    @Sql("""
              UPDATE employee
           SET name = /* employee.userName */'name'
         WHERE id = /* employee.employeeId */0
          """)
    int updateEmployeeReturning(Employee employee);
}