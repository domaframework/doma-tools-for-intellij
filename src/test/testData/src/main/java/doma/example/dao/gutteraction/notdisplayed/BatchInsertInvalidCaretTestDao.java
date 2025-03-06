package doma.example.dao.gutteraction;

import java.util.List;
import doma.example.entity.Employee;
import org.seasar.doma.BatchInsert;
import org.seasar.doma.Dao;

@Dao
interface BatchInsertInvalidCaretTestDao {

 
  @BatchInsert(sqlFile = true)
  int[] NotSQL<caret>ExistError(List<Employee> employees);

}