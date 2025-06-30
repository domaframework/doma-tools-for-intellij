package doma.java.dao;

import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface SourceNameGenerateDao {
  @Select
  Employee generateSQLFile<caret>(Integer id,String name);

}