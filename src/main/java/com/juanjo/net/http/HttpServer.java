  package com.juanjo.net.http;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;


import com.juanjo.net.http.nio.tcpserver.TcpServer;
import com.juanjo.net.http.nio.tcpserver.Event;
import com.juanjo.react.EventStream;
import com.juanjo.net.http.nio.tcpserver.ICommand;
import com.juanjo.net.http.nio.tcpserver.SendCommand;


public class HttpServer extends Thread
{
  private final BiConsumer<Request, Response> handler;
  private final Map<UUID, HttpSession> sessions; 
  private final ServicePool servicePool;
  public HttpServer(BiConsumer<Request, Response> handler)
  {
    this.sessions = new HashMap<>();
    this.servicePool = new ServicePool(4);
    this.handler = handler;
  }

  public void run()
  {
    TcpServer tcpServer = new TcpServer();
    EventStream<Event> requestStream = new EventStream<>(); 
    EventStream<Response> responseStream = new EventStream<>(); 
    
    tcpServer.setEventListener( (evt) -> {
      System.out.println("tcp server event " + evt.getEventType());
      requestStream.send(evt);
    });
    
    requestStream.subscribe((evt) -> {
      System.out.println("event received from tcp server " + evt.getEventType());
      UUID sessionId = evt.getConnection();
      switch(evt.getEventType()) {
        case Event.CONNECT:
          HttpSession s = new HttpSession(sessionId, servicePool.getService());
          s.onRequest(handler);
          s.onResponse((req, resp) -> responseStream.send(resp));
          sessions.put(sessionId, s);
          break;
        case Event.DATA:
          sessions.get(evt.getConnection()).processData(evt.getData());
          break;
        case Event.DISCONNECT:
          System.out.println("Event.DISCONNECT");
          // For the moment we trust GC
          //sessions.get(sessionId).close();
          sessions.remove(sessionId);
          break;
        case Event.COMMAND_RESPONSE:
          break;
        default:
          System.out.println("YOU SHOULD NOT BE HERE");
          break;
      }
    });

    responseStream
      .map((resp) -> {
        return (new SendCommand()
          .setConnection(resp.getSession())
          .setData(HttpUtils.encode(resp)));
      })
      .subscribe((cmd) -> tcpServer.sendCommand(cmd));

    tcpServer.start();

  }

  public class ServicePool
  {
    private final List<ExecutorService> servicePool;
    private int currentService = 0;

    public ServicePool(int size)
    {
      servicePool = this.initializeServicePool(size);
    }

     private List<ExecutorService> initializeServicePool(int size)
    {
      List<ExecutorService> ret = new ArrayList<>();

      for(int i = 0; i < size; i++)
        ret.add(Executors.newSingleThreadExecutor());

      return ret;
    }

    private ExecutorService getService()
    {
      if(currentService < servicePool.size() - 1 )
        currentService = currentService + 1;
      else
        currentService = 0;

      return servicePool.get(currentService);
    }
  }
}

