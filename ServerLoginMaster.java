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
	public DataInputStream[] streamIns = new DataInputStream[5];

	public ServerLoginMaster() throws IOException
	{

	}

	class listen implements Runnable
	{
		public stream[] threads = new stream[5];
		public void run()
		{
			while(true)
			{
				if(serverData.softLogins.isEmpty())
				{
					for(int i = 0; i < 5; i++)
					{
						threads[i] = null;
					}
				}
				else
				{
					for(int i = 0; i < serverData.softLogins.size(); i++)
					{
						if(!threads[i].isAlive())
						{
							threads[i] = new stream(serverData.softLogins.get(i));
							threads[i].start();
						}
						if(i + 1 < 5)
						{
							if(threads[serverData.softLogins.size()].isAlive())
							{
								//threads[i].stop();
							}
						}
					}
				}
			}
		}

		class stream extends Thread
		{
			public Socket socket;
			public DataInputStream streamIn = null;
			public DataOutputStream streamOut = null;
			public stream(Socket p_socket)
			{
				socket = p_socket;
				try
				{
					streamIn = new DataInputStream(socket.getInputStream());
				}
				catch(IOException e)
				{

				}
			}
			public void run()
			{
				while(true)
				{
					try
					{
						System.out.println(streamIn.readUTF());
						System.out.println(streamIn.readUTF());
					}
					catch(IOException e)
					{

					}
				}
			}
		}
	}
}