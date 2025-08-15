package doma.example.dao.sqltofile;

import org.seasar.doma.Dao;
import org.seasar.doma.Sql;
import org.seasar.doma.Function;

@Dao
public interface UnsupportedAnnotationDao {
    @Function
    void unsupported<caret>Method();
}