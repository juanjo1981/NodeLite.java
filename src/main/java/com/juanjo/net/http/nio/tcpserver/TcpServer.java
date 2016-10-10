package com.juanjo.net.http.nio.tcpserver;

import java.lang.Thread;
import java.util.function.Function;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.CancelledKeyException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import java.util.function.Consumer;

public class TcpServer extends Thread
{
  private Selector selectorRef;
  private Consumer<Event> eventListener; 
  private ByteBuffer buffer = ByteBuffer.allocateDirect(1024 * 1024);
  private Map<SocketChannel, UUID> socket2UUID = new HashMap<>();
  private Map<UUID, SocketChannel> UUID2Socket = new HashMap<>();
  private Map<SocketChannel, ConcurrentLinkedQueue<ByteBuffer>> dataToSend = new HashMap<>();

  public TcpServer()
  {
  
  }

  public void setEventListener(Consumer<Event> listener)
  {
    this.eventListener = listener;
  }

  public boolean sendCommand(ICommand cmd)
  {
    if(cmd.getClass().equals(SendCommand.class))
    {
      SendCommand c = (SendCommand) cmd;
      return this.queueDataToSend(c.getConnection(), c.getData());
    }
    if(cmd.getClass().equals(CloseConnectionCommand.class))
    {
      System.out.println("CloseConnectionCommand received");
      CloseConnectionCommand c = (CloseConnectionCommand) cmd;
      return this.deleteSession(UUID2Socket.get(c.getConnection()));
    }
     return true;
    //return this.provisionalInEvent(evt);
  }
  public void run() 
  {
    runServer();
  }

  public void runServer()
  {
    final int DEFAULT_PORT = 1113;

    try(Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open())
    {
      if(serverSocketChannel.isOpen() && selector.isOpen())
      {
        selectorRef = selector;
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 1024 * 1024);
        serverSocketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);

        serverSocketChannel.bind(new InetSocketAddress(DEFAULT_PORT));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while(true)
        {
          selector.select();
          Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
          
          while(keys.hasNext())
          {
            SelectionKey key = keys.next();
            keys.remove();
            try
            {
              if(!key.isValid()) continue;

              if(key.isAcceptable())
              {
                this.acceptOP(key,selector); 
              } 
              else if(key.isReadable())
              {
                this.readOP(key, selector);
              } 
              else if(key.isWritable())
              {
                this.writeOP(key);
              }
            }
            catch(CancelledKeyException ex)
            {
              ex.printStackTrace();
            }
          }
        }
      }
      else
      {
        System.out.println("The server socket channel or selector cannot be opened!");
      }
    
    } catch(IOException ex) {
      System.err.println(ex);
    }
  }

  private void acceptOP(SelectionKey key, Selector selector) throws IOException
  {
    ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
    SocketChannel socketChannel = serverChannel.accept();
    socketChannel.configureBlocking(false);
    
    UUID uuid = this.createSession(socketChannel);
   
    Event evt = new Event(Event.CONNECT)
      .setConnection(uuid);

    eventListener.accept(evt);
    
 
    socketChannel.register(selector, SelectionKey.OP_READ);
  //  System.out.println("Accepted connection " + keepDataTrack.size());
  }

  private void readOP(SelectionKey key, Selector selector) 
  {
    SocketChannel socketChannel = (SocketChannel)key.channel();
    UUID uuid = this.socket2UUID.get(socketChannel);
    if(!socketChannel.isOpen())
    {
      this.deleteSession(socketChannel);

      System.out.println("Trying to readOP a closed channel");
      return;
    }

    //RESCATAR EL ID DEL SOCKET

    buffer.clear();
    int readed = -1;
    try{ readed = socketChannel.read(buffer); } catch( Exception ex) { ex.printStackTrace(); }
    System.out.println( "readed " + readed ); 
    if( readed == -1 ) 
    {
      try{key.cancel();}catch(CancelledKeyException ex) {System.out.println("ES AQUI 4"); ex.printStackTrace() ;}
      try{socketChannel.close();}catch(IOException ex) { ex.printStackTrace(); }
      
      this.deleteSession(socketChannel);

      return;
    }
    buffer.flip();  
    
    ByteBuffer data = ByteBuffer.allocate(buffer.limit());
    data.put(buffer);
    
    Event evt = new Event(Event.DATA)
      .setConnection(uuid)
      .setData(data);
    eventListener.accept(evt);
    
    //try {
    //  key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    //} catch(CancelledKeyException ex) {
    //  this.deleteSession(socketChannel);

    //  System.out.println("ES AQUI 4"); ex.printStackTrace();
    
    //}

  }

  private void writeOP(SelectionKey key) 
  {
    SocketChannel socketChannel = (SocketChannel) key.channel();
    UUID uuid = this.socket2UUID.get(socketChannel);
    if(!socketChannel.isOpen())
    {
      this.deleteSession(socketChannel);
      return;           
    }

    ConcurrentLinkedQueue<ByteBuffer> evts = dataToSend.get(socketChannel);
    
    while(evts.peek() != null)
    {
      try 
      {
        ByteBuffer data = evts.poll();

        data.flip();
        socketChannel.write(data);
        //TODO: lanzar evento de respuesta de comando
      }
      catch(IOException ex)
      {
        //Lanzar evento operacion no completada
        //Lanzar evento socket cerrado
        ex.printStackTrace();
      }
     }
    
    try {
      key.interestOps(SelectionKey.OP_READ);
    }catch(CancelledKeyException ex) {
      this.deleteSession(socketChannel);

      System.out.println("ES AQUI 2"); ex.printStackTrace();
    }

  }
  private boolean queueDataToSend(UUID connection, ByteBuffer data)
  {
    boolean ret = true;
    SocketChannel socket = this.UUID2Socket.get(connection);
    Optional<SelectionKey> optKey = Optional.ofNullable(socket.keyFor(selectorRef));

    if(optKey.isPresent()) { 
      try {
          SelectionKey key  = optKey.get();
          ConcurrentLinkedQueue<ByteBuffer> evts = dataToSend.get(socket);
          evts.offer(data);
          dataToSend.put(socket, evts);
          key.interestOps(SelectionKey.OP_WRITE);
        } catch(CancelledKeyException ex) {
         this.deleteSession(socket);
          ret = false;
          System.out.println("ES AQUI 5"); ex.printStackTrace() ;
        }
    }

    return ret;
  }

  public UUID createSession(SocketChannel socket )
  {
    UUID uuid = UUID.randomUUID();
    this.UUID2Socket.put(uuid, socket);
    this.socket2UUID.put(socket, uuid);
    this.dataToSend.put(socket, new ConcurrentLinkedQueue<ByteBuffer>());
    return uuid;
  }

  public boolean deleteSession(SocketChannel socket)
  {
    boolean ret = true;
    UUID uuid = this.socket2UUID.get(socket);
    
    final SelectionKey key = socket.keyFor(selectorRef);
    this.UUID2Socket.remove(uuid);
    this.socket2UUID.remove(socket);
    
    try {
      if(!key.isValid()) key.cancel();
      if(socket.isOpen()) socket.close();
    } catch(IOException ex)
    {
      ret = false;
      ex.printStackTrace();
    } catch(CancelledKeyException ex) {
      ret = false;
      ex.printStackTrace();
    }

    Event evt = new Event(Event.DISCONNECT)
      .setConnection(uuid);
    eventListener.accept(evt);

    return ret;
  }
}
