import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.*;

public class ServerProcess
{
	public Scanner console = new Scanner(System.in);
	public DataInputStream streamIn = null;
	public DataOutputStream streamOut = null;
	public ArrayList<Socket> socketList = new ArrayList<Socket>();
	public String testUser = "jason";
	public String testPass = "231";
	public ServerProcess(Socket p_socket)
	{
		//Socket socket = listener.accept();
		while(true)
		{
			try 
			{
				System.out.println(p_socket.getRemoteSocketAddress());
				System.out.println("festival");
				streamIn = new DataInputStream(new BufferedInputStream(p_socket.getInputStream()));
				streamOut = new DataOutputStream(p_socket.getOutputStream());
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