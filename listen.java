import java.io.IOException;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.sql.*;

public class listen implements Runnable
{
	public int port = 49152;
	public ServerData serverData = new ServerData();
	public ServerSocketChannel initChannellisten;
	public Selector selector = null;
	public ByteBuffer buffer = ByteBuffer.allocate(256);

	public listen() throws IOException
	{
		initChannellisten = ServerSocketChannel.open();
		initChannellisten.socket().bind(new InetSocketAddress(port));
		initChannellisten.configureBlocking(false);
		selector = Selector.open();
		initChannellisten.register(selector, SelectionKey.OP_ACCEPT);
	}

	//
	// key can write, read, connect and accept
	//
	public void run()
	{
		query();
		while(initChannellisten.isOpen())
		{
			try
			{
				Iterator<SelectionKey> iter;
				SelectionKey key;
				selector.select();
				iter = selector.selectedKeys().iterator();
				while(iter.hasNext())
				{
					key = iter.next();
					iter.remove();
					if(key.isAcceptable())
					{
						//System.out.println("is acceptable");
						handleAccept(key);
						// will go here if detected as an acceptable entry
					}
					else
					{
						// will go here if nothing is coming in
					}
					if(key.isReadable())
					{
						handleRead(key);
						// will go here if detected as a readable entry
					}
					else
					{
						//System.out.println("not readable");
					}
				}
			}
			catch(IOException e)
			{
				System.out.println(" IOException, server of port 49152 terminating, stack trace: " + e);
			}
		}
	}
	//
	// handle writing to ecosystem
	//
	void handleWrite(SelectionKey key)
	{

	}

	//
	// handle accepting clients into eco system
	//
	void handleAccept(SelectionKey key) throws IOException
	{
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		String address = (new StringBuilder( sc.socket().getInetAddress().toString() )).append(":").append( sc.socket().getPort() ).toString();
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, address);
		sc.write(welcomeBuf);
		welcomeBuf.rewind();
		System.out.println("connection from " + address);
	}
	final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to the Server".getBytes());

	//
	// handle reading data into eco system
	//
	void handleRead(SelectionKey key) throws IOException
	{
		SocketChannel ch = (SocketChannel) key.channel();
		StringBuilder sb = new StringBuilder();
		buffer.clear();
		int read = 0;
		while((read = ch.read(buffer)) > 0)
		{
			buffer.flip();
			byte[] bytes = new byte[buffer.limit()];
			buffer.get(bytes);
			sb.append(new String(bytes));
			buffer.clear();
		}
		String msg;
		if(read < 0)
		{
			msg = key.attachment() + " left the chat. \n";
			ch.close();
		}
		else
		{
			msg = key.attachment() + ": " + sb.toString();
		}

		System.out.println(msg);
		broadcast(msg);
	}
	
	//
	// broadcast
	//
	void broadcast(String msg) throws IOException
	{
		ByteBuffer msgBuffer = ByteBuffer.wrap(msg.getBytes());
		for(SelectionKey key : selector.keys())
		{
			if(key.isValid() && key.channel() instanceof SocketChannel)
			{
				SocketChannel sch = (SocketChannel) key.channel();
				sch.write(msgBuffer);
				msgBuffer.rewind();
			}
		}
	}
	void query()
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatBoard?useSSL=false", "root", "313m3n7!");
			Statement statement = conn.createStatement();
			String query = "SELECT * FROM Accounts";

			ResultSet rs = statement.executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			int count = 0;
			String username = "";
			String password = "";
			while(rs.next())
			{
				for(int i = 1; i <= columnsNumber; i++)
				{
					String columnName = rsmd.getColumnName(i);
					String columnValue = rs.getString(i);
					count++;
					if(columnName == "name")
					{
						username = columnValue;
						System.out.println("username " + username);
					}
					else if(columnName == "password")
					{
						password = columnValue;
						System.out.println("password " + password);
					}
					if(count == 2)
					{
						serverData.userBase.put(username, password);
						count = 0;
					}
				}
			}
			for(String key : serverData.userBase.keySet())
			{
				System.out.println(key);
				System.out.println(serverData.userBase.get(key));
			}
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