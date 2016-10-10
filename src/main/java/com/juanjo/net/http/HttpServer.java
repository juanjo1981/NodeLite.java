package com.juanjo.net.http;

import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import com.juanjo.net.http.nio.tcpserver.TcpServer;
import com.juanjo.net.http.nio.tcpserver.Event;
import com.juanjo.react.EventStream;
import com.juanjo.net.http.nio.tcpserver.ICommand;
import com.juanjo.net.http.nio.tcpserver.SendCommand;

public class HttpServer extends Thread
{
  private final BiConsumer<Request, Response> handler;
  private final Map<UUID, HttpSession> sessions; 
  public HttpServer(BiConsumer<Request, Response> handler)
  {
    sessions = new HashMap<>();
    this.handler = handler;
  }

  public void run()
  {
    TcpServer tcpServer = new TcpServer();
    //SessionProxy sessionProxy = new SessionProxy(10,10);
    EventStream<Event> requestStream = new EventStream<>(); 
    EventStream<Response> responseStream = new EventStream<>(); 
    
    tcpServer.setEventListener( (evt) -> {
      System.out.println("tcp server event " + evt.getEventType());
      requestStream.send(evt);
    });
    //sessionProxy.setEventListener( (evt) -> responseStream.send(evt) );
    
    requestStream.subscribe((evt) -> {
      System.out.println("event received from tcp server " + evt.getEventType());
      switch(evt.getEventType()) {
        case Event.CONNECT:
          //sessionProxy.addSession(evt.getConnection(), evt.getData());
          HttpSession s = new HttpSession(evt.getConnection());
          s.onRequest(handler);
          s.onResponse((req, resp) -> responseStream.send(resp));
          sessions.put(evt.getConnection(), s);
          break;
        case Event.DATA:
          sessions.get(evt.getConnection()).processData(evt.getData());
          //sessionProxy.processData(evt.getConnection(), evt.getData());
          break;
        case Event.DISCONNECT:
          System.out.println("Event.DISCONNECT");
          //sessionProxy.deleteSession(evt.getConnection());
          break;
        case Event.COMMAND_RESPONSE:
          //sessionProxy.processResponse(evt.getConnection());
          break;
        default:
          System.out.println("YOU SHOULD NOT BE HERE");
          break;
      }
    });

    responseStream
      .map((resp) -> {
        
        //evt.setData(httpUtils.serializeResponse(resp));
        return (new SendCommand()
          .setConnection(resp.getSession())
          .setData(HttpUtils.encode(resp)));
      })
      .subscribe((cmd) -> tcpServer.sendCommand(cmd));

    tcpServer.start();

  }
}

