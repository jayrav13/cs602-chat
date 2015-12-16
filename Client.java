/*
 *	Client.java
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

	/* Networking */
	boolean sendingdone = false;
	boolean receivingdone = false;
	ObjectOutputStream myOutputStream;
	ObjectInputStream myInputStream;
	Socket socketToServer;
	ChatMessage myObject;
	Scanner scan;

	/* UI Elements */
	Frame newChatWindow;
	Panel northPanel;
	Panel centerPanel;
	Panel loginPanel;
	WhiteBoard whiteboard;
	TextField messageBox;
	TextArea allMessages;
	JList userList;
	TextField usernameTextField;
	JButton loginButton;

	/* Variables */
	String username;
	boolean isLoggedIn = false;
	String[] users;

	/* Development switch */
	boolean isDevelopment = true;


	/* Establish constructor */
	public Client()
	{
		/* Attempt to connect to the server */
		try
		{
			/* Connect to server address and port */
			if(isDevelopment)
			{
				socketToServer = new Socket("127.0.0.1", 8181);
			}
			else
			{
				socketToServer = new Socket("osl1.njit.edu", 8181);
			}
			

			/* Establish streams, start() executes run() */
			myOutputStream = new ObjectOutputStream(socketToServer.getOutputStream());
			myInputStream = new ObjectInputStream(socketToServer.getInputStream());
			start();

		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());	
    }

		/* Create new window */
		newChatWindow = new Frame();
		newChatWindow.setSize(600, 400);
		newChatWindow.setTitle("Chat Client");
		newChatWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				System.exit(0);
			}
		});

		/* Create three panels */
    northPanel = new Panel(new BorderLayout());
    centerPanel = new Panel(new BorderLayout());
    loginPanel = new Panel(new BorderLayout());

    /* Establish user list, messagesm whiteboard */
    allMessages = new TextArea();
		userList = new JList();
		whiteboard = new WhiteBoard();
		whiteboard.setPreferredSize(new Dimension(200, 200));
		centerPanel.add(allMessages, BorderLayout.CENTER);
		centerPanel.add(userList, BorderLayout.WEST);
		centerPanel.add(whiteboard, BorderLayout.EAST);
		newChatWindow.add(centerPanel, BorderLayout.CENTER);

    /* Establish text message text field */
		messageBox = new TextField();
		messageBox.addActionListener(this);
		newChatWindow.add(messageBox, BorderLayout.SOUTH);

		/* Establish login text field */
		usernameTextField = new TextField(30);
		loginPanel.add(usernameTextField, BorderLayout.CENTER);
		loginButton = new JButton("Login");
		loginPanel.add(loginButton, BorderLayout.EAST);
		loginButton.addActionListener(new ActionListener() {
			/* On button press, login / logout */
			public void actionPerformed(ActionEvent e) 
			{
				allMessages.setText("");
				if(!isLoggedIn)
				{
					/* If the user isn't logged in, validate username and then send */
					/* admin-connect, admin-listofusers and admin-history requests */
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

						/* Change elements to reflect */
						allMessages.setText("Welcome to the Chat Room, " + username + "!\n\n");
						loginButton.setText("Logout");
						isLoggedIn = true;
					}
				}
				else
				{
					/* Empty user list */
					users = new String[0];
					userList.setListData(users);

					/* send "bye" message from this user */ 
					myObject = new ChatMessage(username, "bye");
					try
					{
						myOutputStream.reset();
						myOutputStream.writeObject(myObject);
					}
					catch(IOException ioe)
					{
						System.out.println(ioe.getMessage());
					}

					/* Change elements to reflect */
					allMessages.append("Logged out.\n");

					isLoggedIn = false;
					loginButton.setText("Login");
					whiteboard.removeAll();
				}
			}
		});

		/* Add panel, show frame */
		newChatWindow.add(loginPanel, BorderLayout.NORTH);
		newChatWindow.setVisible(true);
	}

	/*	Sends message on enter */
	public void actionPerformed(ActionEvent ae)
	{
		/* Create ChatMessage, reset messageBox text */
		myObject = new ChatMessage(username, messageBox.getText());
		messageBox.setText("");

		/* Send message */
		try
		{
			/* If the message is "bye", change it slightly */
			if(myObject.getMessage().equals("bye"))
			{
				myObject.setMessage(" bye ");
			}

			myOutputStream.reset();
			myOutputStream.writeObject(myObject);
		}
		catch(IOException ioe)
		{
			System.out.println(ioe.getMessage());
		}
	}

	/*	run() function, invoked by start() */
	public void run()
	{
		System.out.println("Listening for messages from server . . . ");
	
		/* While connected, listen for incoming messages */
		try
		{
			while(!receivingdone)
			{
				/* Grab incoming message, handle admin messages */
				myObject = (ChatMessage)myInputStream.readObject();

				/* For list of users, split and add to array, setListData */
				if(myObject.getName().equals("admin-listofusers"))
				{
					String[] response = myObject.getMessage().split("\n");
					for(int i = 0; i < response.length; i++) {
						System.out.println(response[i]);
					}
					userList.setListData(response);
				}
				
				/* Add history to allMessages */
				else if(myObject.getName().equals("admin-history")) 
				{
					allMessages.append(myObject.getMessage());
				}

				/* Draw incoming coordinates */
				else if(myObject.getMessage().equals("draw-coordinates"))
				{
					whiteboard.addLineToWhiteboard(myObject.getName());
				}

				/* Everything else is a regular message, add to allMessages */
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

	/* WhiteBoard class, extends JPanel, implements MouseMotionListener */
	class WhiteBoard extends JPanel implements MouseMotionListener {

		/* Keep track of lastX and lastY for lines */
		int lastX = -1;
		int lastY = -1;

		String[] coordinates;

		/* Default constructor */
		public WhiteBoard() 
		{
			addMouseMotionListener(this);
		}

		/* mouseMoved */
		public void mouseMoved(MouseEvent e)
		{
			// System.out.println("Moved: " + e.getX() + "," + e.getY());
		}

		/* addLineToWhiteboard - take coordinates, split, drawLine */
		public void addLineToWhiteboard(String coords)
		{
			coordinates = coords.split(",");
			getGraphics().drawLine(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]), Integer.parseInt(coordinates[3]));
		}

		/* mouseDragged - send message with coordinates */
		public void mouseDragged(MouseEvent e)
		{

			/* Send message if (lastX, lastY) is not (-1, -1) */
			if(lastX != -1 && lastY != -1) 
			{
				myObject = new ChatMessage((lastX + "," + lastY + "," + e.getX() + "," + e.getY()), "draw-coordinates");

				try {
					myOutputStream.reset();
					myOutputStream.writeObject(myObject);
				}
				catch (IOException ioe) {
					System.out.println(ioe.getMessage());
				}

				
				// System.out.println("Dragged: " + e.getX() + "," + e.getY());
			}
			record(e.getX(), e.getY());
		
		}

		/* Record what the last (x, y) values were */
		private void record(int x, int y)
		{
			lastX = x;
			lastY = y;
		}
	}

	/* main() - create new client */
	public static void main(String[] arg){
		Client c = new Client();
	}
}
