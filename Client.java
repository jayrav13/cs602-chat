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
	boolean sendingdone = false;
	boolean receivingdone = false;
	ObjectOutputStream myOutputStream;
	ObjectInputStream myInputStream;
	Socket socketToServer;
	
	ChatMessage myObject;
	Scanner scan;

	Frame chatWindow;
	TextField messageBox;
	TextArea allMessages;
	TextArea userList;
	Panel mainPanel;

	Frame loginWindow;
	TextField usernameTextField;
	Button loginUser;

	String username;
	boolean isDevelopment = true;

	/*
	 *	Establish constructor.
	 */
	public Client()
	{	

		// Attempt to connect to the server.
		try
		{

			/*
			 * Connect to server address and port. 
			 * Ex: 127.0.0.1
			 * Ex: osl1.njit.edu
			 */
			if(isDevelopment)
			{
				socketToServer = new Socket("127.0.0.1", 8181);
			}
			else
			{
				socketToServer = new Socket("osl1.njit.edu", 8181);
			}
			

			// Establish streams, start() executes run()
			myOutputStream = new ObjectOutputStream(socketToServer.getOutputStream());
			myInputStream = new ObjectInputStream(socketToServer.getInputStream());

			start();
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());	
    }
		
		// Establish chatWindow frame.
		chatWindow = new Frame();
		chatWindow.setSize(600,400);
		chatWindow.setTitle("Chat Client");
		chatWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});

		// Establish login frame.
		loginWindow = new Frame();
		loginWindow.setSize(300, 100);
		loginWindow.setTitle("Log In");
		loginWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});

		// Establish text field.
		messageBox = new TextField();
		messageBox.addActionListener(this);
		
		// Establish text area.
		allMessages = new TextArea();
		userList = new TextArea(10, 10);
		mainPanel = new Panel(new BorderLayout());

		mainPanel.add(userList, BorderLayout.WEST);
		mainPanel.add(allMessages, BorderLayout.EAST);

		// Add text field and text area to frame.
		chatWindow.add(messageBox, BorderLayout.NORTH);
		chatWindow.add(mainPanel, BorderLayout.CENTER);

		usernameTextField = new TextField();
		loginUser = new Button("Log In");
		loginUser.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				if(usernameTextField.getText().length() > 0) 
				{
					username = usernameTextField.getText();

					myObject = new ChatMessage(username, "admin-connect");
					try
					{
						myOutputStream.reset();
						myOutputStream.writeObject(myObject);
					}
					catch(IOException ioe)
					{
						System.out.println(ioe.getMessage());
					}

					myObject.setMessage("admin-listofusers");
					try
					{
						myOutputStream.reset();
						myOutputStream.writeObject(myObject);
					}
					catch(IOException ioe)
					{
						System.out.println(ioe.getMessage());
					}

					loginWindow.setVisible(false);
					allMessages.append("Welcome to the Chat Room, " + username + "!\n\n");
				}
			}
		});
		
		loginWindow.add(loginUser, BorderLayout.SOUTH);
		loginWindow.add(usernameTextField, BorderLayout.NORTH);


		
		// Show frame.
		chatWindow.setVisible(true);
		loginWindow.setVisible(true);
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
		myObject = new ChatMessage(username, messageBox.getText());
		messageBox.setText("");

		/*
		 * Try to reset outputStream, write.
		 * Else, throw message.
		 */
		try
		{
			myOutputStream.reset();
			myOutputStream.writeObject(myObject);

			if(myObject.getMessage().equals("bye"))
			{
				userList.setText("");
				allMessages.setText("Logged out.");
				loginWindow.setVisible(true);
			}

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
				if(myObject.getName().equals("admin-listofusers"))
				{
					userList.setText(myObject.getMessage());
				}
				else 
				{
					allMessages.append(myObject.getName() + ": " + myObject.getMessage() + "\n");
				}
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
