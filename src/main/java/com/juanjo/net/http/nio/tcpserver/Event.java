package com.juanjo.net.http.nio.tcpserver;

import java.util.UUID;
import java.nio.ByteBuffer;

public class Event
{
  public static final int DATA = 0;
  public static final int CONNECT = 1;
  public static final int DISCONNECT = 2;
  public static final int COMMAND_RESPONSE = 3;
  
  private UUID connection;
  private UUID eventId;
  private UUID commandId;
  private int eventType;
  private int status;
  private ByteBuffer data;
 
  
  public Event()
  {
    this(-1);
  }

  public Event(int eventType)
  {
    this.eventId = UUID.randomUUID();
    this.status = 0;
    this.eventType = eventType;
  }

  public Event setConnection(UUID value)
  {
    this.connection = value;
    return this;
  }

  public UUID getConnection()
  {
    return this.connection;
  }

  public Event setEventId(UUID value)
  {
    this.eventId = value;
    return this;
  }

  public UUID getEventId()
  {
    return this.eventId;
  }


  public Event setCommandId(UUID value)
  {
    this.commandId = value;
    return this;
  }

  public UUID getCommandId()
  {
    return this.commandId;
  }

  public Event setEventType(int value)
  {
    this.eventType = value;
    return this;
  }

  public int getEventType()
  {
    return this.eventType;
  }

   public Event setStatus(int value)
  {
    this.status = value;
    return this;
  }

  public int getStatus()
  {
    return this.status;
  }

  public Event setData(ByteBuffer value)
  {
    this.data = value;
    return this;
  }

  public ByteBuffer getData()
  {
    return data;
  }
}

