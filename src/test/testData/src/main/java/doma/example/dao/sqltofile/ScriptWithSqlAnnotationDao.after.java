package doma.example.dao.sqltofile;

import org.seasar.doma.Dao;
import org.seasar.doma.Script;
import org.seasar.doma.Sql;

@Dao
public interface ScriptWithSqlAnnotationDao {
    @Script
    void createTable();
}