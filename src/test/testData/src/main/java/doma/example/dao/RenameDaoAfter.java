package doma.example.dao;

import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface RenameDaoAfter {
  @Select
  Employee renameDaoClassName(Integer id,String name);
}