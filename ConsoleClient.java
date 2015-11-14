/*
 *	Purpose: Provide command line interface for users to chat.
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class ConsoleClient extends Thread
{

	/*
	 *	Establish variables.
	 */
	ChatMessage myObject;
	boolean sendingdone = false; 
	boolean receivingdone = false;
	Scanner scan;
	Socket socketToServer;
	ObjectOutputStream myOutputStream;
	ObjectInputStream myInputStream;

	String username;

	/*
	 *	Establish constructor
	 */
	public ConsoleClient()
	{	
		
		try
		{

			// Set up command line scanner.
			// Create ChatMessage object.
			scan = new Scanner(System.in);
			myObject = new ChatMessage();

			username = "";
			while(username.length() == 0) 
			{
				System.out.print("Hey! What's your name? ");
				username = scan.nextLine();
			}

			System.out.println("Logged in as " + username + ".");

			// Connect to server.
			// Set up input / output streams.
			socketToServer = new Socket("osl1.njit.edu", 8181);
			myOutputStream = new ObjectOutputStream(socketToServer.getOutputStream());
			myInputStream = new ObjectInputStream(socketToServer.getInputStream());

			// start() executes run()
			start();
			
			/*
			 *	While the client is connected, get message from next line.
			 *	Set message as this text, reset the stream and write the object.
			 */
			while(!sendingdone)
			{
				//
				String message = scan.nextLine();
				myObject.setMessage(message);	
				myObject.setName(username);
				myOutputStream.reset();			
				myOutputStream.writeObject(myObject);
			}

			/*
			 *	Once done, close streams and socket.
			 */
			myOutputStream.close();
			myInputStream.close();
      socketToServer.close();	
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			
    }
	}

	/*
	 *	run(), invoked by start()
	 */
	public void run()
	{

		System.out.println("Listening for messages from server . . . ");
		
		/*
		 *	While the user is connected, listen for incoming messages.
		 *	Print incoming messages to the console.
		 */
		try
		{
			while(!receivingdone)
			{
				myObject = (ChatMessage)myInputStream.readObject();
        System.out.println("Messaged received : " + myObject.getMessage());
			}
		}
		catch(IOException ioe)
		{
			System.out.println("IOE: " + ioe.getMessage());
		}
		catch(ClassNotFoundException cnf)
		{
			System.out.println(cnf.getMessage());
		}
	}

	public static void main(String[] arg)
	{
		/*
		 *	Launch the client!
		 */
		ConsoleClient c = new ConsoleClient();
	}
}
