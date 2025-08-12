package doma.example.dao.sqlconversion;

import org.seasar.doma.Dao;
import org.seasar.doma.Script;
import org.seasar.doma.Sql;

@Dao
public interface ScriptWithSqlAnnotationDao {
    @Script
    @Sql("CREATE TABLE test_table (id INT, name VARCHAR(100))")
    void creat<caret>eTable();
}