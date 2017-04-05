import java.net.Socket;
import java.util.*;
import java.io.*;
import java.sql.*;

public class ServerProcess
{
	public String line = "";
	public Scanner console = new Scanner(System.in);
	public DataInputStream streamIn = null;
	public DataOutputStream streamOut = null;
	public ArrayList<Socket> socketList = new ArrayList<Socket>();
	Thread outThread = new Thread();
	Thread inThread = new Thread();

	boolean inputThreadClosed = false;

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

	    	inputThreadClosed = false;
	    	inThread = new Thread(input);
	    	inThread.start();
	    	query();
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
					streamOut.writeUTF(console.nextLine());
	        		streamOut.flush();
	        		if(inputThreadClosed == true)
	        		{
	        			break;
	        		}
	        	}
	        	catch(IOException e)
	        	{
	        		System.out.println(streamOut);
	        		break;
	        	}
			}
			System.out.println("output thread finished");
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
	            	inputThreadClosed = true;
	            	break;
	            }
			}
			System.out.println("input thread finished");
		}
	}

	void query()
	{
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:6000/ChatBoard", "usrname", "pswd");
			Statement statement = conn.createStatement();
			String query = "SELECT * FROM Accounts";
			ResultSet rs = statement.executeQuery(query);
			System.out.println(rs);
		}
		catch(SQLException e)
		{

		}
	}
}