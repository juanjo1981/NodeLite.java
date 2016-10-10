package com.juanjo.react;

import java.util.function.Predicate;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
//import com.juanjo.app.Listener;

public class EventStream<T>
{
  final List<Consumer<T>> listeners;
  private String name;

  public EventStream() 
  {
    listeners = new ArrayList<Consumer<T>>();
  }

  public void setName(String value)
  {
    this.name = value;
  }

  public EventStream<T> filter(Predicate<T> predicate)
  {
    final EventStream<T> out = new EventStream<>();
    out.setName("Filter " + predicate.toString()); 

    final Consumer<T> l = (T t) -> {
      if(predicate.test(t))
        out.send(t);
    };

    this.listeners.add(l);
    return out;
  }

  public <R> EventStream<R> map(Function<T ,R> mapper)
  {
    final EventStream<R> out = new EventStream<>();
    out.setName("map " + mapper.toString()); 
    
    final Consumer<T> l = (T t) -> {
      R r = mapper.apply(t);
      out.send(r);
    };

    this.listeners.add(l);

    return out;
  }
  
  public EventStream<T> subscribe(Consumer<T> consumer)
  {
    System.out.println("Subscription on " + name + " " + Thread.currentThread().getName() );

    final Consumer<T> l = (T t) -> {
      consumer.accept(t);
    };

    this.listeners.add(l);

    return this;
  }
  public void send(final T t)
  {
    //System.out.println("Sending to " + name + " " + t.toString() + " Listeners " + listeners.size());
    this.listeners.forEach(consumer -> consumer.accept(t));
  
  }
}
