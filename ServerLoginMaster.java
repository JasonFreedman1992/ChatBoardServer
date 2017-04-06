import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.*;
import java.io.*;

public class ServerLoginMaster
{
	public ServerData serverData = new ServerData();
	public listen listen = new listen();

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
						System.out.println("empty");
					}
					else if(!serverData.softLogins.isEmpty())
					{
						Thread.sleep(1000);
						System.out.println("not empty");
					}
					//Thread.sleep(1000);
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
			}
		}
	}

	class SubServer
	{
		Socket socket;
		public SubServer(Socket p_socket)
		{
			socket = p_socket;
		}
		//ServerProcess process = new ServerProcess(next);
	}
}