package doma.example.dao;

import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface RenameDaoMethodWithoutSql {
  @Insert
  int notExistSql<caret>(Employee employee);
}