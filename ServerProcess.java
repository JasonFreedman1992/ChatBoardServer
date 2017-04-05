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
	    	inThread = new Thread(input)
;	    	inThread.start();
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
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatBoard?useSSL=false", "root", "313m3n7!");
			System.out.println("made it past conn");
			Statement statement = conn.createStatement();
			String query = "SELECT * FROM Accounts";
			ResultSet rs = statement.executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			while(rs.next())
			{
				for(int i = 1; i <= columnsNumber; i++)
				{
					if(i > 1) System.out.print(", ");
					String columnValue = rs.getString(i);
					System.out.println(columnValue + " " + rsmd.getColumnName(i));
				}
				System.out.println("");
			}
			System.out.println(rs.toString());
		}
		catch(SQLException e)
		{
			System.out.println("no connection");
		}
		catch(ClassNotFoundException e)
		{
			System.out.println("not found class");
		}
	}
}