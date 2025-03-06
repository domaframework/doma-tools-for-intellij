package doma.example.dao.gutteraction;

import doma.example.entity.Employee;
import org.seasar.doma.Dao;
import org.seasar.doma.Insert;
import org.seasar.doma.Sql;

@Dao
interface InsertGutterTestDao {

  @Insert
  int nonExistSQLFile(Employee employee);

  @Insert
  @Sql("insert into employee (id, name) values (/* employee.detail.detail.detaiDemolname */0, /* employee.name */'')")
  int nonExistSQLFileAndTemplateIncluded(Employee employee);

  @Insert(sqlFile = true)
  int nonExistSQLFileError(Employee employee, String orderBy);

  @Insert(sqlFile = true)
  int existsSQLFile1(Employee employee, String orderBy);

  @Insert(sqlFile = true)
  int existsSQL<caret>File2(Employee employee, String orderBy);
}