package doma.example.dao.gutteraction;

import java.util.List;
import doma.example.entity.Employee;
import org.seasar.doma.BatchInsert;
import org.seasar.doma.Dao;

@Dao
interface BatchInsertGutterTestDao {

  
  @BatchInsert
  int[] nonExistSQLFile(List<Employee> employees);

 
  @BatchInsert(sqlFile = true)
  int[] existsSQLFile1(List<Employee> employees);

 
  @BatchInsert(sqlFile = true)
  int[] existsSQL<caret>File2(List<Employee> employees);

 
  @BatchInsert(sqlFile = true)
  int[] existsSQLFile3(List<Employee> employees);
}