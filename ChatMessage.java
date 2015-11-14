/*
 *	Purpose: Create the message sent to users.
 *	Extends Serializable, similar to DataObject.
 */

import java.io.*;

public class ChatMessage implements Serializable
{

	public String name;
	public String message;
	
	public ChatMessage()
	{
		/*
		 *	Nothing. Default constructor.
		 */
	}

	/*
	 *	Create message.
	 */
	public ChatMessage(String name, String message)
	{
		setName(name);
		setMessage(message);
	}

	/*
	 *	Accessor / Mutator methods for name / message.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getMessage()
	{
		return message;
	}

}