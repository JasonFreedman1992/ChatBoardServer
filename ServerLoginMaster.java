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
						Thread.sleep(1000);
						System.out.println("rsa" + serverData.softLogins.get(0).getRemoteSocketAddress() + " is connected");
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