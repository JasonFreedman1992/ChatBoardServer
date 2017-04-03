import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Server
{
	public static void main(String[] args) throws IOException
	{
		ServerSocket listener = new ServerSocket(49152);
		ArrayList<Socket> socketList = new ArrayList<Socket>();
		try
		{
			while(true)
			{
				Socket socket = listener.accept();
				
				try
				{
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					out.println(socket.getRemoteSocketAddress().toString());
					System.out.println(socket.getRemoteSocketAddress());
				}
				finally
				{
					socket.close();
				}
			}
		}
		finally
		{
			listener.close();
		}
		//System.out.println("Hello World");
	}
}