package doma.example.collector;

import java.util.stream.Collector;

public class HogeCollector implements Collector<String, String, String> {

    @Override
    public Supplier<String> supplier() {
      return null;
    }

    @Override
    public BiConsumer<String, String> accumulator() {
      return null;
    }

    @Override
    public BinaryOperator<String> combiner() {
      return null;
    }

    @Override
    public Function<String, String> finisher() {
      return null;
    }

    @Override
    public Set<Characteristics> characteristics() {
      return null;
    }
  }