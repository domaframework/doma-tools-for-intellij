package doma.example.dao.gutteraction;

import org.seasar.doma.Dao;
import org.seasar.doma.Script;
import org.seasar.doma.Sql;

@Dao
interface ScriptInvalidCaretTestDao {

  @Script
  void existsSQLFile1();

  @Script
  void nonExistSQLFileError();

  @Script
  void nonExistSQLFile();

  @Sql("create table employee (id int, name varchar(10))")
  @Script
  void NoSqlFile<caret>WithTemplate();

  @Script
  void existsSQLFile2();
}