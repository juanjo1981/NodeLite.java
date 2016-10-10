package com.juanjo.net.http.nio.tcpserver;

import java.util.UUID;
import java.nio.ByteBuffer;

public class SendCommand implements ICommand
{
  private UUID connection;
  private ByteBuffer data;

  public UUID getConnection()
  {
    return this.connection;
  }
  public SendCommand setConnection(UUID conn)
  {
    this.connection = conn;
    return this;
  }

  public ByteBuffer getData()
  {
    return this.data;
  }
  public SendCommand setData(ByteBuffer data)
  {
    this.data = data;
    return this;
  }
}

