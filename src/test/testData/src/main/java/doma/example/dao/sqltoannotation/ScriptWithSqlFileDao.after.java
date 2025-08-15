package doma.example.dao.sqltoannotation;

import org.seasar.doma.Dao;
import org.seasar.doma.Script;
import org.seasar.doma.Sql;

@Dao
public interface ScriptWithSqlFileDao {
    @Script
    @Sql("""
            CREATE TABLE employee
              (
                     id INTEGER PRIMARY KEY
                 , name VARCHAR(100)
                 , age INTEGER
              ) ;
            CREATE INDEX idx_employee_name
            ON employee(name) ;
            """)
    void createTables();
}