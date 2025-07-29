package doma.example.dao.formatter;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Select;
import org.seasar.doma.Sql;

@Dao
public interface SqlInjectionDao {
    @Select
    @Sql("""
SELECT *
  FROM tableName
 WHERE id = 0""")
    Employee selectInjection();

    @Select
    @Sql("""
SELECT *
  FROM tableName
 WHERE id = /* id */1""")
    Employee selectInjection2(Integer id);
}