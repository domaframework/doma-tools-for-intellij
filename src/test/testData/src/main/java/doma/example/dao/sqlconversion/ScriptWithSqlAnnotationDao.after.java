package doma.example.dao.sqlconversion;

import org.seasar.doma.Dao;
import org.seasar.doma.Script;
import org.seasar.doma.Sql;

@Dao
public interface ScriptWithSqlAnnotationDao {
    @Script
    void createTable();
}