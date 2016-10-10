package com.juanjo;

import com.juanjo.net.http.HttpServer;
import com.juanjo.net.http.Request;
import com.juanjo.net.http.Response;

public class App 
{
  public static void main( String[] args ) throws Exception
  {
    HttpServer server = new HttpServer((req, resp) -> {
          System.out.println("SERVER HANDLER");
          
          req.onData((data) -> {
            System.out.println("onData " + new String(data));
          });
          
          req.onEnd((data) ->{
            System.out.println("onEnd request");
         });

        resp.getHeader().setStatusCode(200);
        resp.getHeader().setHttpVersion("HTTP/1.1");
        resp.getHeader().setReasonPhrase("Success");
        resp.getHeader().getData().put("Content-Type","text/html; charset=utf-8");
        resp.appendBody("<!DOCTYPE html><html><body><h1>My First Heading</h1><p>My first paragraph.</p></body></html>\r\n".getBytes());
        resp.end();

        });
    
    server.start();
  }
}



