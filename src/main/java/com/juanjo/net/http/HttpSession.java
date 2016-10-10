package com.juanjo.net.http;

import java.util.UUID;
import java.util.ArrayList;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.function.BiConsumer;
import java.nio.ByteBuffer;

public final class HttpSession
{
  private HttpTransaction current;
  private final Queue<HttpTransaction> transactions;
  private final UUID sessionId;
  private BiConsumer<Request, Response> on_request;
  private BiConsumer<Request, Response> on_response;

  public HttpSession(UUID sessionId)
  {
    this.sessionId = sessionId;
    transactions = new ArrayDeque<>();
  }

  public void onRequest(BiConsumer<Request, Response> consumer)
  {
    this.on_request = consumer;
  }

  public void onResponse(BiConsumer<Request, Response> consumer)
  {
    this.on_response = consumer;
  }

  public final void processData(ByteBuffer data)
  {
    HttpRequestTokenizer tokenizer = new HttpRequestTokenizer(data);
     
    while(tokenizer.hasNext()) {
      HttpRequestTokenizer.Token t = tokenizer.next();
      if(t.isError()) {
      
      }
      if(t.isHeader()) {
        //Falta comproobar si el current request actual es completo y meterlo en la lista de transactions
        Request req = new Request(this.sessionId).setHeader(t.getHeader());

        Response resp = new Response(this.sessionId);
        resp.onEnd((Void) ->  {
          on_response.accept(req, resp);
        });

        current = new HttpTransaction()
          .setRequest(req)
          .setResponse(resp);

        this.on_request.accept(current.getRequest(), current.getResponse());
      }
      if(t.isData()) {
        current.getRequest().appendBody(t.getData());
      }
      if(t.isEnd()) {
        //current.getRequest().appendBody(t.getData());
      }
    }
    
  }

  public final class HttpTransaction
  {
    private Request request;
    private Response response;

    public Request getRequest() 
    {
      return request;
    }
    public HttpTransaction setRequest(Request value)
    {
      this.request = value;
      return this;
    }

    public Response getResponse()
    {
      return response;
    }

    public HttpTransaction setResponse(Response value)
    {
      this.response = value;
      return this;
    }
  }
}



