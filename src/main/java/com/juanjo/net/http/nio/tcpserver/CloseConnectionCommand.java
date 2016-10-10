package com.juanjo.net.http.nio.tcpserver;

import java.util.UUID;

public class CloseConnectionCommand implements ICommand
{
  private UUID connection;

  public UUID getConnection()
  {
    return this.connection;
  }
  public CloseConnectionCommand setConnection(UUID conn)
  {
    this.connection = conn;
    return this;
  }

   
}

