package com.juanjo.net.http;

import java.util.Map;
import java.util.HashMap;

public class ResponseHeader
{
  private Map<String, String> data = new HashMap<>();
  private int statusCode;
  private String httpVersion;
  private String reasonPhrase;

  public Map<String, String> getData()
  {
    return this.data;
  }
  public int getStatusCode() 
  {
    return this.statusCode;
  }
  public void setStatusCode(int value) 
  {
    this.statusCode = value;
  }

  public String getHttpVersion()
  {
    return this.httpVersion;
  }
  public void setHttpVersion(String value)
  {
    this.httpVersion = value;
  }

  public String getReasonPhrase()
  {
    return this.reasonPhrase;
  }
  public void setReasonPhrase(String value)
  {
    this.reasonPhrase = value;
  }


}
