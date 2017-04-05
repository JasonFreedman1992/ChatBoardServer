import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.*;

public class ServerProcess
{
	public String line = "";
	public Scanner console = new Scanner(System.in);
	public DataInputStream streamIn = null;
	public DataOutputStream streamOut = null;
	public ArrayList<Socket> socketList = new ArrayList<Socket>();
	Thread outThread = new Thread();
	Thread inThread = new Thread();

	output output = new output();
	input input = new input();

	public ServerProcess(Socket p_socket) throws IOException
	{
		System.out.println(p_socket.getRemoteSocketAddress());
		System.out.println("festival");
		streamOut = new DataOutputStream(p_socket.getOutputStream());
		streamIn = new DataInputStream(new BufferedInputStream(p_socket.getInputStream()));
	    try
	    {
	    	outThread = new Thread(output);
	    	outThread.start();

	    	inThread = new Thread(input);
	    	inThread.start();
	    }
	    catch(Exception e)
	    {

	    }
	}
	class output implements Runnable
	{
		public void run()
		{
			while(true)
			{
				try
				{
					System.out.println(streamOut);
					streamOut.writeUTF(console.nextLine());
	        		streamOut.flush();
	        	}
	        	catch(IOException e)
	        	{
	        		break;
	        	}
			}
		}
	}

	class input implements Runnable
	{
		public void run()
		{
			while(true)
			{
				try
				{
					String input = streamIn.readUTF();
	            	System.out.println(input);
				}
	            catch(IOException e)
	            {
	            	break;
	            }
			}
		}
	}
}