package com.juanjo.net.http;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.UUID;
import java.io.ByteArrayOutputStream;

public class Response
{
  private ResponseHeader header = new ResponseHeader();
  private ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
  private final UUID session;

  private Consumer<Void> on_end;

  public Response(final UUID session)
  {
    this.session = session;
  }

  public UUID getSession()
  {
    return this.session;
  }

  public Response setHeader(ResponseHeader header)
  {
    this.header = header;
    return this;
  }

  public ResponseHeader getHeader()
  {
    return this.header;
  }

  public void appendBody(byte[] data)
  {
    bodyStream.write(data, 0, data.length);
  }
  
  public byte [] getBody()
  {
    return bodyStream.toByteArray(); 
  }

  public void end()
  {
    this.on_end.accept(null);
  }

  public void onEnd(Consumer<Void> handler)
  {
    this.on_end = handler;
  }
}

