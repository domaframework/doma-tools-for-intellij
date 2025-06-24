package doma.example.dao.inspection.returntype;

import org.seasar.doma.*;
import java.sql.*;
import java.util.List;

@Dao
public interface FactoryReturnTypeTestDao {

  @ArrayFactory(typeName = "integer")
  Array createIntegerArray(Integer[] elements);

  @ArrayFactory(typeName = "text")
  Array createString2DArray(String[][] elements);

  @ArrayFactory(typeName = "text")
  List<String> <error descr="The return type must be \"java.sql.Array\"">createStringList</error>(Integer[] elements);

  @BlobFactory
  Blob createBlob();

  @BlobFactory
  Integer <error descr="The return type must be \"java.sql.Blob\"">createBlobWithInteger</error>();

  @ClobFactory
  Clob createClob();

  @ClobFactory
  Integer <error descr="The return type must be \"java.sql.Clob\"">createClobWithInteger</error>();

  @NClobFactory
  NClob createNClob();

  @NClobFactory
  Integer <error descr="The return type must be \"java.sql.NClob\"">createNClobWithInteger</error>();

  @SQLXMLFactory
  SQLXML createSQLXML();

  @SQLXMLFactory
  Integer <error descr="The return type must be \"java.sql.SQLXML\"">createSQLXMLWithInteger</error>();
}