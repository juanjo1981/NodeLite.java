package com.juanjo.net.http;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.BiConsumer;

public interface AsyncHandler 
{
  public default <A, B> BiConsumer<A, B> makeAsync(BiConsumer<A, B> consumer, ExecutorService executor)
  {
    return (a, b) -> executor.submit(() -> consumer.accept(a,b));
  }

  public default <A> Consumer<A> makeAsync(Consumer<A> consumer, ExecutorService executor)
  {
    return (a) -> executor.submit(() -> consumer.accept(a));
  }

}

