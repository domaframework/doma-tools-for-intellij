package doma.example.dao.sqltofile;

import doma.example.User;
import org.seasar.doma.BatchUpdate;
import org.seasar.doma.Dao;
import org.seasar.doma.Sql;
import java.util.List;

@Dao
public interface BatchUpdateWithSqlAnnotationDao {
    @BatchUpdate
    @Sql("UPDATE users SET name = /* users.name */'test' WHERE id = /* users.id */1")
    int[] batchUpda<caret>te(List<User> users);
}