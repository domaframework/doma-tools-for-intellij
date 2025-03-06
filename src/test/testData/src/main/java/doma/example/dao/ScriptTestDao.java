package doma.example.dao;

import doma.example.entity.*;
import org.seasar.doma.*;

@Dao
interface ScriptTestDao {
  
  @Script
  void existsSQLFile();
  
  @Script
  void <error descr="SQL file does not exist">nonExistSQLFileError1</error>();
  
  @Script
  void <error descr="SQL file does not exist">nonExistSQLFileError2</error>();
  
  @Sql("create table employee (id int, name varchar(10))")
  @Script
  void nonExistSQLFileAndTemplateIncluded();
}