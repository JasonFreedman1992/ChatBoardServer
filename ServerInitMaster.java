import java.io.IOException;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;

public class ServerInitMaster
{
	public listen listen = new listen();
	public int port;
	public ServerSocketChannel initChannellisten;
	public SocketChannel initChannel;
	public ServerData serverData = new ServerData();
	public ServerInitMaster(int p_port) throws IOException
	{
		port = p_port;
	}

	class listen implements Runnable
	{
		public void run()
		{
			try
			{
				initChannellisten = ServerSocketChannel.open();
				initChannellisten.socket().bind(new InetSocketAddress(port));
				while(true)
				{
					System.out.println("listening... ");
					initChannel = initChannellisten.accept();
					serverData.Q.add(initChannel);
					System.out.println("initialized...");
					System.out.println(initChannel.socket().getRemoteSocketAddress());
					System.out.println(serverData.Q.size());
				}
			}
			catch(IOException e)
			{
				
			}
			// while(true)
			// {
			// 	try
			// 	{
			// 		System.out.println("listening...");
			// 		next = listener.accept();
			// 		System.out.println("initialized... ");
			// 		if(next.isBound())
			// 		{
			// 			System.out.println(next.getRemoteSocketAddress());
			// 		}
			// 	}
			// 	catch(IOException e)
			// 	{
			// 		System.out.println(e);
			// 	}
			// }
		}
	}
}