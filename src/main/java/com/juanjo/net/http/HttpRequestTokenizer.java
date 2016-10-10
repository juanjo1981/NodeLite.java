package com.juanjo.net.http;

import java.nio.ByteBuffer;

import com.juanjo.net.http.RequestHeader;

public class HttpRequestTokenizer 
{
  private ByteBuffer data;
  private Token current;
  public HttpRequestTokenizer(ByteBuffer data) 
  {
    current = new Token();
    this.data = data;
    this.data.rewind();
  }

  public final boolean hasNext()
  {
    return (!current.isEnd() || (data.position() < data.limit()));
  }

  public final Token next()
  {
    current = new Token();

    if(data.position() == data.limit() ) {
      current.setType(Token.END);
      return current;
    }

    if(hasHeader(data)) {
      StringBuilder headerRaw = new StringBuilder();
      boolean headerComplete = false;
      int crlf = 0;
    
      for(int i = data.position(); i < data.limit() && !headerComplete; i++)
      {
        char c = (char)data.get();
        headerRaw.append(c);
       
        crlf = ((int)c == 10 || (int)c == 13) ? (crlf + 1) : 0;
        if(crlf == 4)
        {
          headerComplete = true;
          current.setType(Token.HEADER);
          current.setHeader(parseRequestHeader(headerRaw));
        }
      }

    }
    else {
      current.setType(Token.DATA);
      byte [] tokenData = new byte[data.limit() - data.position()];
      data.get(tokenData);
      current.setData(tokenData);
    }

    return current; 
  }

  public static boolean hasHeader(ByteBuffer data)
  {
    StringBuilder sb = new StringBuilder();
    String firstLine;
    boolean isHeader = false;

    for(int i = data.position(); i < 10 && i < data.limit(); i++)
    {
      char c = (char)data.get(i);
      if((int)c == 10 || (int)c == 13) 
        break;
      
      sb.append(c);
    }
    
    firstLine = sb.toString().toUpperCase();
    String [] methods = {"OPTIONS", "GET", "HEAD", "POST", "PUT", "DELETE", "TRACE", "CONNECT"};

    for(String method : methods)
    {
      if(firstLine.contains(method.toUpperCase()))
      {
        isHeader = true;
        break;
      }
    }
    return isHeader;
  }
  
    private static RequestHeader parseRequestHeader(CharSequence raw)
    {
      RequestHeader header = new RequestHeader(); 
      String [] tokens = raw.toString().split("\n");
      
      String [] requestLine = tokens[0].split(" ");
      if(requestLine.length == 3)
      {
        header.setMethod(requestLine[0]);
        header.setRequestUri(requestLine[1]);
        header.setHttpVersion(requestLine[2]);
      }
      else
      {
        //Devolver error
      }

      for(int i = 1; i < tokens.length; i++)
      {
        String [] headerField =tokens[i].split(":");
        if(headerField.length == 2) header.getData().put(headerField[0].trim(), headerField[1].trim());
        if(headerField.length > 2) header.getData().put(headerField[0].trim(), headerField[1].trim() + ":" + headerField[2].trim()  );
      }
      
      long contentLength = (header.getData().get("Content-Length") != null) ? Long.parseLong(header.getData().get("Content-Length")) : 0;

      header.setContentLength(contentLength);
   
      return header;
    }



  public final class Token
  {
    private static final int ERROR = -1;
    private static final int HEADER = 0;
    private static final int DATA = 1;
    private static final int END = 2;
    private static final int NOTYPE = 100;
    private int type;
    private RequestHeader header;
    private byte [] data;

    public Token()
    {
      this.type = NOTYPE;
    }
    public void setType(int value)
    {
      this.type = value;
    }
    public final boolean isError()
    {
      return type == ERROR;
    }
    public final boolean isHeader()
    {
      return type == HEADER; 
    }
    public final boolean isData()
    {
      return type == DATA;
    }
    public final boolean isEnd()
    {
      return type == END;
    }
    public void setHeader(RequestHeader value)
    {
      this.header = value;
    }
    public RequestHeader getHeader()
    {
      return header;
    }
    public void setData(byte [] value)
    {
      data = value;
    }
    public byte [] getData()
    {
      return data;
    }
  }

}
