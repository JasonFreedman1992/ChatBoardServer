import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class Server
{
	public static void main(String[] args) throws IOException
	{
		Socket socket = null;
		ServerSocket listener = new ServerSocket(49152);
		while(true)
		{
			socket = listener.accept();
			if(socket.isBound())
			{
				ServerProcess process = new ServerProcess(socket);
			}	
		}
	}
}