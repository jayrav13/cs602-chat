/*
 *	Purpose: Provide GUI for users to chat.
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
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

	Frame newChatWindow;
	TextField messageBox;
	TextArea allMessages;
	Panel northPanel;
	Panel centerPanel;
	Panel loginPanel;
	JList userListList;

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

		// New Window
		newChatWindow = new Frame();
		newChatWindow.setSize(600, 400);
		newChatWindow.setTitle("Chat Client");
		newChatWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});

    northPanel = new Panel(new BorderLayout());
    centerPanel = new Panel(new BorderLayout());
    loginPanel = new Panel(new BorderLayout());

    // Establish text area.
    allMessages = new TextArea();
		userListList = new JList();
		centerPanel.add(allMessages, BorderLayout.EAST);
		centerPanel.add(userListList, BorderLayout.WEST);
		newChatWindow.add(centerPanel, BorderLayout.CENTER);

    // Establish text field.
		messageBox = new TextField();
		messageBox.addActionListener(this);
		newChatWindow.add(messageBox, BorderLayout.NORTH);

		usernameTextField = new TextField(60);
		loginPanel.add(usernameTextField, BorderLayout.WEST);
		loginUser = new Button("Login");
		loginPanel.add(loginUser, BorderLayout.EAST);
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

					myObject.setMessage("admin-history");
					try
					{
						myOutputStream.reset();
						myOutputStream.writeObject(myObject);
					}
					catch(IOException ioe)
					{
						System.out.println(ioe.getMessage());
					}

					allMessages.append("Welcome to the Chat Room, " + username + "!\n\n");
				}
			}
		});
		newChatWindow.add(loginPanel, BorderLayout.SOUTH);

		// Show frame.
		newChatWindow.setVisible(true);
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
				allMessages.append("Logged out.\n");
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
					String[] response = myObject.getMessage().split("\n");
					for(int i = 0; i < response.length; i++) {
						System.out.println(response[i]);
					}
					userListList.setListData(response);
				}
				else if(myObject.getName().equals("admin-history")) 
				{
					allMessages.append(myObject.getMessage());
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
