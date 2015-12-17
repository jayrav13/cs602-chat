/*
 *  ChatServer.java
 *  Purpose: Manage incoming messages, broadcast accordingly.
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer
{  
  public static void main(String[] args) 
  {  

    /* ArrayList: holds all ChatHandlers */
    ArrayList<ChatHandler> AllHandlers = new ArrayList<ChatHandler>();
    int socketNumber = 8181;

    try
    {
      /* Set socket to socketNumber. This can be changed to anything 1024 - 65535 */
      ServerSocket s = new ServerSocket(socketNumber);

      /* Infinite loop */
      for (;;)
      {
        /* Accept incoming, start thread */
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


/*  ChatHandler: keep track of all chat clients, communicate accordingly */
class ChatHandler extends Thread
{
  /*  Default constructor. Accepts socket and reference to ChatHandler */
  public ChatHandler(Socket i, ArrayList<ChatHandler> h) 
  {
    /* Attempt to create streams */
 	  incoming = i;
	  handlers = h;

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

  /*  Attempts to take received message and send to everyone */
  public synchronized void broadcast()
  {
    /* If the message is bye, handler is removed */
    if(myObject.getMessage().equals("bye"))
    {
      done = true;
      myObject.setMessage("admin-listofusers");
      handlers.remove(this);
    }

    /* For all admin messages */
    if(myObject.getMessage().contains("admin-"))
    {
      done = false;

      /* On connect, add handler */
      if(myObject.getMessage().equals("admin-connect"))
      {
        handlers.add(this);
        System.out.println("OUTGOING: /connected username = " + myObject.getName() + ", message = " + myObject.getMessage() + ", count = " + handlers.size());
      }

      /* When requesting list of users, gather and send to all users */
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
            /* Handler left unexpectedly, remove */
            left = handler;
          }
        }
        handlers.remove(left);
      }

      /* When requesting history, grab text file data and send to requesting handler only */
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

    /* If this isn't an admin or "bye" message, regular chat message, send to all */
    else 
    {
      ChatHandler left = null;
  
      if(!myObject.getMessage().equals("draw-coordinates"))
      {
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
      }


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
          /* Handler left unexpectedly, remove */
          left = handler;
        }
      }
      
      handlers.remove(left);
    }

  }

  /* Listen for messages */
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
  
  /* All Variables */
  ChatMessage myObject = null;
  private Socket incoming;

  boolean done = false;
  String myUsername;
  ArrayList<ChatHandler> handlers;

  ObjectOutputStream out;
  ObjectInputStream in;

  PrintWriter writer;
}

