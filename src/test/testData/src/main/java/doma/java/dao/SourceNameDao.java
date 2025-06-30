package doma.java.dao;

import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface SourceNameDao {
  @Select
  Employee generateSQLFile(Integer id,String name);

  @Select
  Employee <caret>jumpToDaoFile(Integer id);

  @Select
  Employee existsSQLFile1(Integer id, String name);
}