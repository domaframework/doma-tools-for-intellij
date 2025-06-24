package doma.example.dao.inspection.paramtype;

import org.seasar.doma.*;
import java.sql.*;
import java.util.List;

@Dao
public interface FactoryParamTestDao {

  @ArrayFactory(typeName = "integer")
  Array createIntegerArray(Integer[] elements);

  @ArrayFactory(typeName = "integer")
  Array <error descr="The number of parameters must be one">create</error>();

  @ArrayFactory(typeName = "text")
  Array createString2DArray(String[][] elements);

  @ArrayFactory(typeName = "text")
  Array createStringList(List<String> <error descr="The parameter type must be an array type">elements</error>);

  @BlobFactory
  Blob createBlob();

  @BlobFactory
  Blob <error descr="The number of parameters must be zero">createBlobWithParam</error>(Integer id);

  @ClobFactory
  Clob createClob();

  @ClobFactory
  Clob <error descr="The number of parameters must be zero">createClobWithParam</error>(Integer id);

  @NClobFactory
  NClob createNClob();

  @NClobFactory
  NClob <error descr="The number of parameters must be zero">createNClobWithParam</error>(Integer id);

  @SQLXMLFactory
  SQLXML createSQLXML();

  @SQLXMLFactory
  SQLXML <error descr="The number of parameters must be zero">createSQLXMLWithParam</error>(Integer id);
}