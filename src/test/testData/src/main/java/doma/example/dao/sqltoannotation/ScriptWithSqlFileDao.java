package doma.example.dao.sqltoannotation;

import org.seasar.doma.Dao;
import org.seasar.doma.Script;

@Dao
public interface ScriptWithSqlFileDao {
    @Script
    void createTables();
}