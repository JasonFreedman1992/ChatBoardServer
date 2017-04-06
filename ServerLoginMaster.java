import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class ServerLoginMaster
{
	ServerSocket listener;
	Socket next;
	public ServerLoginMaster(int p_port) throws IOException
	{
		listener = new ServerSocket(p_port);
	}

	public void listen() throws IOException
	{
		next = listener.accept();
		if(next.isBound())
		{
			ServerProcess process = new ServerProcess(next);
		}
	}
}