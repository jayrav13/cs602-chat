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
	WhiteBoard whiteboard;
	JList userList;

	TextField usernameTextField;
	JButton loginButton;

	String username;
	boolean isLoggedIn = false;
	boolean isDevelopment = true;

	String[] users;

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
		userList = new JList();
		whiteboard = new WhiteBoard();
		whiteboard.setPreferredSize(new Dimension(200, 200));
		centerPanel.add(allMessages, BorderLayout.CENTER);
		centerPanel.add(userList, BorderLayout.WEST);
		centerPanel.add(whiteboard, BorderLayout.EAST);
		newChatWindow.add(centerPanel, BorderLayout.CENTER);

    // Establish text field.
		messageBox = new TextField();
		messageBox.addActionListener(this);
		newChatWindow.add(messageBox, BorderLayout.SOUTH);

		usernameTextField = new TextField(30);
		loginPanel.add(usernameTextField, BorderLayout.CENTER);
		loginButton = new JButton("Login");
		loginPanel.add(loginButton, BorderLayout.EAST);
		loginButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				loginButton.setText("Logout");
				allMessages.setText("");
				if(!isLoggedIn)
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

						allMessages.setText("Welcome to the Chat Room, " + username + "!\n\n");

						isLoggedIn = true;
					}
				}
				else
				{
					isLoggedIn = false;
					loginButton.setText("Login");
					users = new String[0];
					userList.setListData(users);

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

					allMessages.append("Logged out.\n");
				}
			}
		});
		newChatWindow.add(loginPanel, BorderLayout.NORTH);

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
				users = new String[0];
				userList.setListData(users);
				allMessages.append("Logged out.\n");
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
					String[] response = myObject.getMessage().split("\n");
					for(int i = 0; i < response.length; i++) {
						System.out.println(response[i]);
					}
					userList.setListData(response);
				}
				else if(myObject.getName().equals("admin-history")) 
				{
					allMessages.append(myObject.getMessage());
				}
				else if(myObject.getMessage().equals("draw-coordinates"))
				{
					whiteboard.addLineToWhiteboard(myObject.getName());
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
	class WhiteBoard extends JPanel implements MouseMotionListener {
		int lastX = -1;
		int lastY = -1;

		public WhiteBoard() 
		{
			addMouseMotionListener(this);
		}

		public void mouseMoved(MouseEvent e)
		{
			System.out.println("Moved: " + e.getX() + "," + e.getY());
		}

		public void addLineToWhiteboard(String coords)
		{
			String[] coordinates = coords.split(",");

			getGraphics().drawLine(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]), Integer.parseInt(coordinates[3]));
		}

		public void mouseDragged(MouseEvent e)
		{
			if(lastX + lastY != -2) {
				// getGraphics().drawLine(lastX, lastY, e.getX(), e.getY());
			}

			myObject = new ChatMessage((lastX + "," + lastY + "," + e.getX() + "," + e.getY()), "draw-coordinates");

			try {
				myOutputStream.reset();
				myOutputStream.writeObject(myObject);
			}
			catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}

			record(e.getX(), e.getY());

			System.out.println("Dragged: " + e.getX() + "," + e.getY());
		}

		private void record(int x, int y)
		{
			lastX = x;
			lastY = y;
		}
	}

	public static void main(String[] arg){
	
		/*
		 *	Launch new client!
		 */
		Client c = new Client();

	}
}

