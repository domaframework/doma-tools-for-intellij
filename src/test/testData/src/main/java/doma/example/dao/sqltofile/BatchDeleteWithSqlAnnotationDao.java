package doma.example.dao.sqltofile;

import org.seasar.doma.BatchDelete;
import org.seasar.doma.Dao;
import org.seasar.doma.Sql;
import java.util.List;

@Dao
public interface BatchDeleteWithSqlAnnotationDao {
    @BatchDelete
    @Sql("DELETE FROM users WHERE id = /* ids */1")
    int[] batch<caret>Delete(List<Integer> ids);
}