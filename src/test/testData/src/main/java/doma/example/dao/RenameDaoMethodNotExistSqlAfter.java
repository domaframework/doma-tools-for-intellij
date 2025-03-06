package doma.example.dao;

import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface RenameDaoMethodNotExistSql {
  @Select
  Employee renameDaoMethodNameAfter(Integer id, String name);
}