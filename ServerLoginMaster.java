import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.*;
import java.io.*;

public class ServerLoginMaster
{
	Scanner console = new Scanner(System.in);
	public ServerData serverData = new ServerData();
	public listen listen = new listen();
	public DataOutputStream streamOut = null;

	public ServerLoginMaster() throws IOException
	{

	}

	class listen implements Runnable
	{
		public void run()
		{
			while(true)
			{
				try
				{
					if(serverData.softLogins.isEmpty())
					{
						Thread.sleep(1000);
						System.out.println("no soft logins to process");
					}
					else if(!serverData.softLogins.isEmpty())
					{
						for(int i = 0; i < serverData.softLogins.size(); i++)
						{
							LinkedList<Thread> threads = new LinkedList<Thread>();
							threads.add(new Thread(new SubServer(i)));
							threads.get(i).start();
						}
					}
				}
				catch(Exception e)
				{
					System.out.println(e);
					try
					{
						Thread.sleep(1000);
					}
					catch(Exception f)
					{

					}
				}
			}
		}
		class SubServer implements Runnable
		{	
			int id;
			public DataInputStream streamIn = null;

			public SubServer(int p_id)
			{
				id = p_id;
			}
			public void run()
			{
				while(true)
				{
				try
				{
					streamIn = new DataInputStream(serverData.softLogins.get(id).getInputStream());
					if(!streamIn.readUTF().equals(""))
					{
						System.out.println(streamIn.readUTF());
					}
				}
				catch(IOException e)
				{

				}
				}
			}
		}
	}
}

// streamIn = new DataInputStream(serverData.softLogins.get(i).getInputStream());
// if(streamIn.readUTF().equals(""))
// {

// }
// else
// {
// 	System.out.println(streamIn.readUTF() + " " + streamIn.readUTF());
// }