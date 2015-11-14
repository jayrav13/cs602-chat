/*
 *	Purpose: Provide GUI to users to chat.
 */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client extends Thread implements ActionListener
{

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

	public Client(){	
		
		f = new Frame();
		f.setSize(300,400);
		f.setTitle("Chat Client");
		f.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent we){
				System.exit(0);
			}
		});
		tf = new TextField();
		tf.addActionListener(this);
		f.add(tf, BorderLayout.NORTH);
		ta = new TextArea();
		f.add(ta, BorderLayout.CENTER);
		
		try{						

			//scan = new Scanner(System.in);

			//myObject = new ChatMessage();

			socketToServer = new Socket("127.0.0.1", 4000);

			myOutputStream =
				new ObjectOutputStream(socketToServer.getOutputStream());

			myInputStream =
				new ObjectInputStream(socketToServer.getInputStream());
			start();
			
				
		}
		catch(Exception e){
			System.out.println(e.getMessage());	
        }
		f.setVisible(true);
	}
	public void actionPerformed(ActionEvent ae){
		myObject = new ChatMessage();
		myObject.setMessage(tf.getText());
		tf.setText("");
		try{
			myOutputStream.reset();
			myOutputStream.writeObject(myObject);
		}catch(IOException ioe){
			System.out.println(ioe.getMessage());
		}
		
	}
	public void run(){
		System.out.println("Listening for messages from server . . . ");
		try{
			while(!receivingdone){
				myObject = (ChatMessage)myInputStream.readObject();
               	ta.append(myObject.getMessage() + "\n");

			}
		}catch(IOException ioe){
			System.out.println("IOE: " + ioe.getMessage());
		}catch(ClassNotFoundException cnf){
			System.out.println(cnf.getMessage());
		}
	}

	public static void main(String[] arg){
	
		Client c = new Client();

	}
}
