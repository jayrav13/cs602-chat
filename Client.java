/*
 *	Purpose: Provide GUI for users to chat.
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client extends Thread implements ActionListener
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
	Frame f;
	TextField tf;
	TextArea ta;

	/*
	 *	Establish constructor.
	 */
	public Client()
	{	
		
		// Establish frame.
		f = new Frame();
		f.setSize(300,400);
		f.setTitle("Chat Client");
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});

		// Establish text field.
		tf = new TextField();
		tf.addActionListener(this);
		
		// Establish text area.
		ta = new TextArea();

		// Add text field and text area to frame.
		f.add(tf, BorderLayout.NORTH);
		f.add(ta, BorderLayout.CENTER);
		
		// Attempt to connect to the server.
		try
		{

			/*
			 * Connect to server address and port. 
			 * Ex: 127.0.0.1
			 * Ex: afs1.njit.edu
			 */
			socketToServer = new Socket("127.0.0.1", 4000);

			// Establish streams, start() executes run()
			myOutputStream = new ObjectOutputStream(socketToServer.getOutputStream());
			myInputStream = new ObjectInputStream(socketToServer.getInputStream());
			start();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());	
    }
		
		// Show frame.
		f.setVisible(true);
	}

	/*
	 *	Sends message on enter.
	 */
	public void actionPerformed(ActionEvent ae)
	{
		/*
		 * Create ChatMessage object, set message as 
		 * ...text from text field. Reset text field.
		 */
		myObject = new ChatMessage();
		myObject.setMessage(tf.getText());
		tf.setText("");

		/*
		 * Try to reset outputStream, write.
		 * Else, throw message.
		 */
		try
		{
			myOutputStream.reset();
			myOutputStream.writeObject(myObject);
		}
		catch(IOException ioe)
		{
			System.out.println(ioe.getMessage());
		}
	}

	/*
	 *	run() function, invoked by start()
	 */
	public void run()
	{
		System.out.println("Listening for messages from server . . . ");
	
		/*
		 *	While the user is connected, listen for incoming messages.
		 *	Write the messages to the text area.
		 */
		try
		{
			while(!receivingdone)
			{
				myObject = (ChatMessage)myInputStream.readObject();
        ta.append(myObject.getMessage() + "\n");
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

	public static void main(String[] arg){
	
		/*
		 *	Launch new client!
		 */
		Client c = new Client();

	}
}
