package doma.example.dao.sqlconversion;

import doma.example.User;
import org.seasar.doma.BatchInsert;
import org.seasar.doma.Dao;
import org.seasar.doma.Sql;
import java.util.List;

@Dao
public interface BatchInsertWithSqlAnnotationDao {
    @BatchInsert
    @Sql("INSERT INTO users (name, email) VALUES (/* users.name */'test', /* users.email */'test@example.com')")
    int[] batchInse<caret>rt(List<User> users);
}