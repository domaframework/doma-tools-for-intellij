package doma.example.dao.quickfix;

import org.seasar.doma.Dao;
import org.seasar.doma.Script;
import org.seasar.doma.Sql;

@Dao
interface ScriptQuickFixTestDao {

  @Script
  void existsSQLFile();

  @Script
  void generateSQLFile<caret>();

  @Script
  void <error descr="SQL file does not exist">nonExistSQLFileError</error>();

  @Sql("create table employee (id int, name varchar(10))")
  @Script
  void nonExistSQLFileAndTemplateIncluded();
}