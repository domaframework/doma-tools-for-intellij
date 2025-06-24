package doma.example.function;

import org.seasar.doma.jdbc.Config;
import org.seasar.doma.jdbc.PreparedSql;

import java.io.ObjectInputFilter;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class HogeBiFunction implements BiFunction<Config, PreparedSql, String> {

  @Override
  public String apply(Config config, PreparedSql preparedSql) {
    return "";
  }
}