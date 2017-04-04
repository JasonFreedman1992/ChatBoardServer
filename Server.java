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

		ArrayList<Socket> socketList = new ArrayList<Socket>();
		String testUser = "jason";
		String testPass = "231";


		Socket socket = listener.accept();
		try 
		{
			streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			boolean hold = true;
			while(hold)
			{
				try
				{
					String line = streamIn.readUTF();
					System.out.println(line);
					hold = line.equals(".bye");
				}
				catch(IOException e)
				{
					hold = false;
				}
			}
			// if(socket != null)
			// {
			// 	socket.close();
			// }
			// if(streamIn != null)
			// {
			// 	streamIn.close();
			// }
            //PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            //out.println("Jin is cute");
        } 
       	finally 
        {
           socket.close();
        }
		System.out.println(socket.getRemoteSocketAddress());
		System.out.println("festival");
	}
}