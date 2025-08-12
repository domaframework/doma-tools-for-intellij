package doma.example.dao.sqlconversion;

import doma.example.User;
import org.seasar.doma.BatchUpdate;
import org.seasar.doma.Dao;
import org.seasar.doma.Sql;
import java.util.List;

@Dao
public interface BatchUpdateWithSqlAnnotationDao {
    @BatchUpdate(sqlFile = true)
    int[] batchUpdate(List<User> users);
}