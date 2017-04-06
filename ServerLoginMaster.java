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
	public Thread[] threads = new Thread[5];

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
							threads[i] = new Thread(new SubServer(i));
							threads[i].start();
						}
					}
				}
				catch(Exception e)
				{
					//System.out.println(e);
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
			int id = 0;
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
						if(serverData.softLogins.size() > 0)
						{
						streamIn = new DataInputStream(serverData.softLogins.get(id).getInputStream());
						System.out.println(streamIn.readUTF());
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