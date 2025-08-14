package doma.example.dao.sqltofile;

import doma.example.User;
import org.seasar.doma.BatchInsert;
import org.seasar.doma.Dao;
import org.seasar.doma.Sql;
import java.util.List;

@Dao
public interface BatchInsertWithSqlAnnotationDao {
    @BatchInsert(sqlFile = true)
    int[] batchInsert(List<User> users);
}