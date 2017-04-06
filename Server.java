import java.io.IOException;
import java.net.ServerSocket;
import java.io.*;

public class Server
{
	public static void main(String[] args) throws IOException
	{
		ServerLoginListener loginListener = new ServerLoginListener(49152);
		loginListener.start();
	}
}