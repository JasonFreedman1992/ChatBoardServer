import java.io.IOException;
import java.net.ServerSocket;
import java.io.*;

public class Server
{
	public static void main(String[] args) throws IOException
	{
		ServerLoginMaster loginListener = new ServerLoginMaster(49152);
		loginListener.listen();
	}
}