import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;


public class Server
{
	public static void main(String[] args) throws IOException
	{
		Scanner console = new Scanner(System.in);
		ServerSocket listener = new ServerSocket(49152);
		ArrayList<Socket> socketList = new ArrayList<Socket>();
		String testUser = "jason";
		String testPass = "231";

		try
		{
			boolean hold = true;
			while(hold)
			{
				Socket socket = listener.accept();
				//socketList.add(socket);
				try 
				{
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    String password = console.next();
                    out.println("Jin is cute");
                } 
               	finally 
                {
                   socket.close();
                }
				System.out.println(socket.getRemoteSocketAddress());
				System.out.println("festival");
			}
		}
		finally
		{
			listener.close();
		}
	}
}