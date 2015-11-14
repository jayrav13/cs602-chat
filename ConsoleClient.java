/*
 *	Purpose: Provide GUI to users to chat.
 */

import java.io.*;
import java.net.*;
import java.util.*;

public class ConsoleClient extends Thread
{

	ChatMessage myObject;
	boolean sendingdone = false, receivingdone = false;
	Scanner scan;
	Socket socketToServer;
	ObjectOutputStream myOutputStream;
	ObjectInputStream myInputStream;

	public ConsoleClient(){	
		
		try
		{						

			scan = new Scanner(System.in);

			myObject = new ChatMessage();

			socketToServer = new Socket("127.0.0.1", 4000);

			myOutputStream = new ObjectOutputStream(socketToServer.getOutputStream());

			myInputStream = new ObjectInputStream(socketToServer.getInputStream());

			start();
			
			while(!sendingdone)
			{

				String message = scan.nextLine();

				myObject.setMessage(message);	
				
				myOutputStream.reset();			

				myOutputStream.writeObject(myObject);
				
				//if(myObject.getMessage().equals("bye")){
				//	sendingdone = true;
				//}
			}

			myOutputStream.close();
			
			myInputStream.close();

      socketToServer.close();	
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
			
    }
	}
	public void run()
	{
		System.out.println("Listening for messages from server . . . ");
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
		ConsoleClient c = new ConsoleClient();
	}
}
