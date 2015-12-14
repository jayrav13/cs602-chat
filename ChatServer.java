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
  public static void main(String[] args) 
  {  

    //  ArrayList: holds all ChatHandlers
    ArrayList<ChatHandler> AllHandlers = new ArrayList<ChatHandler>();
	
    try 
    {  
      //  Set socket to 8181. This can be changed to anything 1025 - 65536.
      ServerSocket s = new ServerSocket(8181);
      
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
     *  Attempt to create streams.
     */

 	  incoming = i;
	  handlers = h;
	  // handlers.add(this);

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
    /*
     *  This ends the run() loop and displays number of remaining handlers.
     */
    if(myObject.getMessage().equals("bye"))
    {
      done = true;
      myObject.setMessage("admin-listofusers");
      handlers.remove(this);
    }

    if(myObject.getMessage().contains("admin-"))
    {
      done = false;
      if(myObject.getMessage().equals("admin-connect"))
      {
        handlers.add(this);
        System.out.println("OUTGOING: /connected username = " + myObject.getName() + ", message = " + myObject.getMessage() + ", count = " + handlers.size());
        /*
         *  Do nothing. Handler added.
         */
      }
      else if(myObject.getMessage().equals("admin-listofusers"))
      {
        String output = "User List\n\n";
        for(ChatHandler handler : handlers)
        {
          output = output + handler.myUsername + "\n";
        }
        myObject.setMessage(output.substring(0, output.length() - 1));
        ChatHandler left = null;

        for(ChatHandler handler : handlers)
        {
          myObject.setName("admin-listofusers");
          ChatMessage cm = new ChatMessage(myObject.getName(), myObject.getMessage().substring(0, myObject.getMessage().length()));
          try
          {
            handler.out.writeObject(cm);
            System.out.println("OUTGOING: /message username = " + myObject.getName() + ", message = " + myObject.getMessage());
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
      }
      else if(myObject.getMessage().equals("admin-history"))
      {
        String output = "";
        try
        {
          File file = new File("history.txt");
          Scanner scanner = new Scanner(file);
          while(scanner.hasNext())
          {
            output = output + scanner.nextLine() + "\n";
          }
          scanner.close();

          try
          {
            for(ChatHandler handler : handlers)
            {
              if(handler.myUsername.equals(myObject.getName()))
              {
                ChatMessage cm = new ChatMessage("admin-history", output);
                handler.out.writeObject(cm);
                System.out.println("OUTGOING: /history username = " + myObject.getName());
              }
            }
          }
          catch(IOException ioe)
          {
            System.out.println(ioe.getMessage());
          }

        }
        catch(FileNotFoundException e)
        {
          System.out.println(e.getMessage());
        }
      }
    }
    else 
    {
      ChatHandler left = null;
  
      try
      {
        FileWriter fw = new FileWriter("history.txt", true);
        fw.write(myObject.getName() + ": " + myObject.getMessage() + "\n");
        fw.close();
      }
      catch(IOException ioe)
      {
        System.out.println(ioe.getMessage());
      }

      /*
       *  For each ChatHandler, take my message and write that message out.
       */
      for(ChatHandler handler : handlers)
      {
        ChatMessage cm = new ChatMessage(myObject.getName(), myObject.getMessage());
        try
        {
          handler.out.writeObject(cm);
          System.out.println("OUTGOING: /message username = " + handler.myUsername + ", message = " + myObject.getMessage());
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
    }

  }

  /*
   *  Primary function that listens for messages.
   */
  public void run()
  {
    System.out.println("Started...");
  	try
    { 	
      while(!done)
      {
        myObject = (ChatMessage)in.readObject();
        myUsername = myObject.getName();
        System.out.println("INCOMING: username = " + myObject.getName() + ", message = " + myObject.getMessage());
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
  String myUsername;
  ArrayList<ChatHandler> handlers;

  ObjectOutputStream out;
  ObjectInputStream in;

  PrintWriter writer;
}

