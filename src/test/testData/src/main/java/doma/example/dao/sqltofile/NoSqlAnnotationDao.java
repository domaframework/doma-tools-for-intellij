package doma.example.dao.sqltofile;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;

@Dao
public interface NoSqlAnnotationDao {
    @Insert
    User inser<caret>t(Employee employee);
}