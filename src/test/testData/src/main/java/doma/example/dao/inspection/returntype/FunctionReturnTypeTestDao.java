package doma.example.dao.inspection.returntype;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.seasar.doma.Dao;
import org.seasar.doma.Function;
import org.seasar.doma.In;
import org.seasar.doma.InOut;
import org.seasar.doma.Out;
import org.seasar.doma.ResultSet;
import doma.example.entity.*;
import org.seasar.doma.jdbc.Reference;

@Dao
public interface FunctionReturnTypeTestDao {

  @Function
  int primitiveFunction(@In int id);

  @Function
  void voidFunction(@In int id);

  @Function
  long primitiveLongFunction(@In long id);

  @Function
  boolean primitiveBooleanFunction(@In boolean flag);

  @Function
  double primitiveDoubleFunction(@In double value);

  @Function
  float primitiveFloatFunction(@In float value);

  @Function
  byte primitiveByteFunction(@In byte value);

  @Function
  short primitiveShortFunction(@In short value);

  @Function
  char <error descr="The return type char is invalid">primitiveCharFunction</error>(@In char value);

  @Function
  String executeFunction(
      @In Integer arg1, @InOut Reference<Integer> arg2, @Out Reference<Integer> arg3);

  @Function
  Packet entityFunction(
      @In Packet arg1, @InOut Reference<Packet> arg2, @Out Reference<Packet> arg3);

  @Function
  List<String> listFunction(@ResultSet List<String> arg1);

  @Function
  List<Packet> immutableEntityListFunction(@ResultSet List<Packet> arg1);

  @Function
  List<Pckt> entityListFunction(@ResultSet List<Pckt> arg1);

  @Function
  Optional<String> optionalFunction(
      @In Optional<Integer> arg1,
      @InOut Reference<Optional<Integer>> arg2,
      @Out Reference<Optional<Integer>> arg3);

  @Function
  List<Optional<String>> listOptionalFunction(@ResultSet List<Optional<String>> arg1);

  @Function
  List<Optional<Packet>> entityOptionalListFunction(@ResultSet List<Optional<Packet>> arg1);

  @Function
  Map<String, Object> mapFunction(@ResultSet List<Optional<Packet>> arg1);

  @Function
  Optional<Map<String, Object>> mapOptionalFunction(@ResultSet List<Optional<Packet>> arg1);

  @Function
  List<Map<String, Object>> mapListFunction(@ResultSet List<Optional<Packet>> arg1);

  @Function
  List<Optional<Map<String, Object>>> mapOptionalListFunction(@ResultSet List<Optional<Packet>> arg1);

  @Function
  Optional<MyEnum> enumFunction(
      @In Optional<MyEnum> arg1,
      @InOut Reference<Optional<MyEnum>> arg2,
      @Out Reference<Optional<MyEnum>> arg3);

  @Function
  List<Optional<MyEnum>> listEnumFunction(@ResultSet List<Optional<MyEnum>> arg1);

  enum MyEnum {}

}
