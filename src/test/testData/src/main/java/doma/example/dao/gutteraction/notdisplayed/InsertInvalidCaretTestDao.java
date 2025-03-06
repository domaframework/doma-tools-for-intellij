package doma.example.dao.gutteraction;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Sql;

@Dao
interface InsertInvalidCaretTestDao {

  @Insert
  @Sql("insert into employee (id, name) values (/* employee.detail.detail.detaiDemolname */0, /* employee.name */'')")
  int NoSqlFile<caret>WithTemplate(Employee employee);

  @Insert(sqlFile = true)
  int existsSQLFile1(Employee employee, String orderBy);

  @Insert(sqlFile = true)
  int NoSql<caret>File(Employee employee, String orderBy);
}