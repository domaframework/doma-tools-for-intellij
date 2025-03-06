package doma.example.dao;

import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface RenameDaoMethod {
  @Select
  Employee renameDaoMethodName<caret>(Integer id,String name);
}