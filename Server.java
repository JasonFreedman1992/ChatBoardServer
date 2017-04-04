import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.*;


public class Server
{
	public static void main(String[] args) throws IOException
	{
		ServerSocket listener = new ServerSocket(49152);
		DataInputStream streamIn = null;
		DataOutputStream streamOut = null;
		Scanner console = new Scanner(System.in);

		ArrayList<Socket> socketList = new ArrayList<Socket>();
		String testUser = "jason";
		String testPass = "231";


		//Socket socket = listener.accept();
		while(true)
		{
			try 
			{
				Socket socket = listener.accept();
				System.out.println(socket.getRemoteSocketAddress());
				System.out.println("festival");
				streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				streamOut = new DataOutputStream(socket.getOutputStream());
				boolean hold = true;
				while(hold)
				{
					try
					{
						String line = streamIn.readUTF();
						System.out.println(line);
						if(line == ".bye")
						{
							hold = false;
						}
						String output = console.nextLine();
						streamOut.writeUTF(output);
						streamOut.flush();
					}
					catch(IOException e)
					{
						hold = false;
					}
				}
	        } 
	        catch(IOException e)
	        {

	        }
    	}
	}
}