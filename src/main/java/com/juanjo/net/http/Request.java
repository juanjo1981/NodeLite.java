package com.juanjo.net.http;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.UUID;
import java.io.ByteArrayOutputStream;

public class Request
{
  private RequestHeader header;
  private ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
  private long contentReceived = 0;
  
  private Consumer<byte []> on_data;
  private Consumer<byte []> on_end;
  private final UUID session;
  
  public Request(final UUID session)
  {
    this.session = session;
  }

  public UUID getSession()
  {
    return this.session;
  }

  public Request setHeader(RequestHeader header)
  {
    this.header = header;
    return this;
  }

  public RequestHeader getHeader()
  {
    return header;
  }

  public long getContentReceived()
  {
    return contentReceived;
  }

  public Request setContentReceived(long value)
  {
    contentReceived = value;
    return this;
  }
  
  public void onData(Consumer<byte []> consumer)
  {
    this.on_data = consumer;
  }

  public void onEnd(Consumer<byte []> consumer)
  {
    this.on_end = consumer;
  }

  public void appendBody(byte[] data)
  {
    if(data != null && data.length > 0) {
      bodyStream.write(data, 0, data.length);
      this.contentReceived = this.contentReceived + data.length;
    }

    if(this.isComplete()) 
      this.end(data);
    else
      this.on_data.accept(data);
  }

  public void end(byte[] data)
  {
    this.on_end.accept(data);
  }

  public boolean isComplete()
  {
    return (this.header != null  && (this.contentReceived == this.header.getContentLength()));
  }
}

