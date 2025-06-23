package doma.example.function;

import java.util.function.Function;
import java.util.stream.Stream;

public class HogeFunction implements Function<Stream<String>, String> {

  @Override
  public String apply(Stream<String> t) {
    return null;
  }
}