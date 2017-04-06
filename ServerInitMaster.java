import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.*;
import java.io.*;

public class ServerInitMaster
{
	public DataOutputStream streamOut = null;
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
					next = listener.accept();
					if(next.isBound())
					{
						
					}
					next = null;
				}
				catch(IOException e)
				{
					System.out.println(e);
				}
			}
		}
	}
}