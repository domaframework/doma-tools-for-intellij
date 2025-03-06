package doma.example.dao;

import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface RenameDao<caret> {
  @Select
  Employee renameDaoClassName(Integer id,String name);
}