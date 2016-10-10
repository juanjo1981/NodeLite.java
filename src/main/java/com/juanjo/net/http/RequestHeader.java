package com.juanjo.net.http;

import java.util.Map;
import java.util.HashMap;

public class RequestHeader
{
  private Map<String, String> data = new HashMap<>();

  private long contentLength = 0;

  private String method;
  private String requestUri;
  private String httpVersion; 

  public String getMethod()
  {
    return method;
  }
  public String getRequestUri()
  {
    return requestUri;
  }
  public String getHttpVersion() 
  {
    return httpVersion;
  }
  public RequestHeader setMethod(String value)
  {
    method = value;
    return this;
  }
  public RequestHeader setRequestUri(String value)
  {
    requestUri = value;
    return this;
  }
  public RequestHeader setHttpVersion(String value) 
  {
    httpVersion = value;
    return this;
  }

  public long getContentLength()
  {
    return contentLength;
  }
  
  public RequestHeader setContentLength(long value)
  {
    contentLength = value;
    return this;
  }

  public Map<String, String> getData()
  {
    return data;
  }

  public RequestHeader setData(Map<String, String> value)
  {
    this.data = data;
    return this;
  }

    

}

