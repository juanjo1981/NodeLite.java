package com.juanjo.net.http;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Iterator;

public class HttpUtils
{
  public static ByteBuffer encode(Response resp)
  {
    StringBuilder sb = new StringBuilder();
    sb.append(resp.getHeader().getHttpVersion()) 
      .append(" ")
      .append(resp.getHeader().getStatusCode())
      .append(" ")
      .append(resp.getHeader().getReasonPhrase())
      .append("\r\n");

    Iterator it = resp.getHeader().getData().entrySet().iterator();

    while (it.hasNext()) 
    {
      Map.Entry pair = (Map.Entry)it.next();
      sb.append(pair.getKey())
        .append(": ")
        .append(pair.getValue())
        .append("\r\n");
    }

    //TODO: This connection closed its temporal. The connection SHOULD be closed by the client or by a session manager
    sb.append("Content-Length: "+ resp.getBody().length + "\r\n");
    sb.append("Connection: Closed\r\n");
    sb.append("\r\n");
    //sb.append(resp.getBody());

    byte [] header = sb.toString().getBytes();
    byte [] body = resp.getBody();

    ByteBuffer ret = ByteBuffer.allocate(header.length + body.length);
    ret.put(header);
    ret.put(body);
    
    
    return ret;

  }


}
