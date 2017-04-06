import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.*;
import java.io.*;

public class ServerInitMaster
{
	public ServerData serverData = new ServerData();
	public DataOutputStream streamOut = null;
	private ServerSocket listener;
	public listen listen = new listen();
	public Socket next;
	public DataInputStream streamIn = null;

	public ServerInitMaster(int p_port) throws IOException
	{
		listener = new ServerSocket(p_port);
	}

	class listen implements Runnable
	{
		public void run()
		{
			while(true)
			{
				try
				{
					System.out.println("listening...");
					next = listener.accept();
					System.out.println("initialized... ");
					if(next.isBound())
					{
						System.out.println(next.getRemoteSocketAddress());
						serverData.softLogins.add(next);
						System.out.println(serverData.softLogins.size());
					}
				}
				catch(IOException e)
				{
					System.out.println(e);
				}
			}
		}
	}
}