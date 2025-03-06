package doma.example.dao;

import java.util.List;
import doma.example.entity.*;
import org.seasar.doma.*;


@Dao
interface BatchInsertTestDao {

  @BatchInsert
  int[] nonExistSQLFile(List<Employee> employees);

  @BatchInsert(sqlFile = true)
  @Sql("insert into employee (id, name) values (/* employees.employeeId */0, /* employees.name */'name')")
  int[] nonExistSQLFileAndTemplateIncluded(List<Employee> employees);

  @BatchInsert(sqlFile = true)
  int[] <error descr="SQL file does not exist">nonExistSQLFileError</error>(List<Employee> employees);

  @BatchInsert(sqlFile = true)
  int[] existsSQLFile(List<Employee> employees);
}