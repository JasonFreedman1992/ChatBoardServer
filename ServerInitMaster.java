import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.*;
import java.io.*;

public class ServerInitMaster
{
	public ServerData serverData = new ServerData();
	private ServerSocket listener;
	public listen listen = new listen();
	public Socket next;

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