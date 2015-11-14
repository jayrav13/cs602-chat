/*
 *  Purpose: Manage incoming messages, broadcast accordingly.
 */

import java.io.*;
import java.net.*;
import java.util.*;

/*
 *  ChatServer: handle incoming requests.
 */
public class ChatServer
{  
  public static void main(String[] args ) 
  {  

    //  ArrayList: holds all ChatHandlers
    ArrayList<ChatHandler> AllHandlers = new ArrayList<ChatHandler>();
	
    try 
    {  
      //  Set socket to 4000. This can be changed to anything 1025 - 65536.
      ServerSocket s = new ServerSocket(4000);
      
      //  On a loop forever.
      for (;;)
      {
        //  Create an incoming socket, which accepts the request from the server socket.
        //  Create an anonymous object, ChatHandler, which receives AllHandlers as a
          //  reference and will be added accordingly.
        // .start() invokes run().
        Socket incoming = s.accept( );
        new ChatHandler(incoming, AllHandlers).start();
      }   
    }
    catch (Exception e) 
    {  
      System.out.println(e);
    } 
  } 
}

/*
 *  ChatHandler: keep track of all chat clients, communicate accordingly.
 */
class ChatHandler extends Thread
{
  /*
   *  Default constructor. Accepts socket and reference to ChatHandler. 
   */
  public ChatHandler(Socket i, ArrayList<ChatHandler> h) 
  {
    /*
     *  Add self to handlers. Attempt to create streams.
     */

 		incoming = i;
	  handlers = h;
	  handlers.add(this);

	  try
    {
	  	in = new ObjectInputStream(incoming.getInputStream());
	  	out = new ObjectOutputStream(incoming.getOutputStream());
  	}
    catch(IOException ioe)
    {
  		System.out.println("Could not create streams.");
  	}
  }

  /*
   *  Attempts to take received message and send to everyone.
   */
  public synchronized void broadcast()
  {

  	ChatHandler left = null;
  
    /*
     *  For each ChatHandler, take my message and write that message out.
     */
  	for(ChatHandler handler : handlers)
    {
   	 	ChatMessage cm = new ChatMessage();
  		cm.setMessage(myObject.getMessage());
  		try
      {
  			handler.out.writeObject(cm);
  			System.out.println("Writing to handler outputstream: " + cm.getMessage());
  		}
      catch(IOException ioe)
      {
        /*
         *  This exception is thrown when one of the client is no longer available.
         *  In that case, just remove the handler.
         */
  			left = handler;
  		}
  	}
  	
    handlers.remove(left);
  	
    /*
     *  Wait for special message "bye" to force hang up client.
     *  This ends the run() loop and displays number of remaining handlers.
     */
  	if(myObject.getMessage().equals("bye"))
  	{
      done = true;	
  		handlers.remove(this);
  		System.out.println("Removed handler. Number of handlers: " + handlers.size());
  	}
  	
    System.out.println("Number of handlers: " + handlers.size());
  }

  /*
   *  Primary function that listens for messages.
   */
  public void run()
  {  
  	try
    { 	
  		while(!done)
      {
        /*
         *  When I type in a message, collect it and broadcast it.
         */
  			myObject = (ChatMessage)in.readObject();
  			System.out.println("Message read: " + myObject.getMessage());
  			broadcast();
  		}			    
  	} 
    catch (IOException e)
    {  
  	  if(e.getMessage().equals("Connection reset"))
      {
  	    System.out.println("A client terminated its connection.");
  		}
      else
      {
  	    System.out.println("Problem receiving: " + e.getMessage());
  		}
  	}
    catch(ClassNotFoundException cnfe)
    {
  		System.out.println(cnfe.getMessage());
  	}
    finally
    {
  		handlers.remove(this);
  	}
  }
   
  ChatMessage myObject = null;
  private Socket incoming;

  boolean done = false;
  ArrayList<ChatHandler> handlers;

  ObjectOutputStream out;
  ObjectInputStream in;
}

