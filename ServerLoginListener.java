import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public class ServerLoginListener
{
	ServerSocket listener;
	Socket next;
	public ServerLoginListener(int p_port) throws IOException
	{
		listener = new ServerSocket(p_port);
	}

	public void start() throws IOException
	{
		next = listener.accept();
		if(next.isBound())
		{
			ServerProcess process = new ServerProcess(next);
		}
	}
}