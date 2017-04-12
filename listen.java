import java.io.IOException;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.sql.*;
import javax.swing.JOptionPane;


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
		mapDatabase();
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
						handleAccept(key);
					}
					else
					{

					}
					if(key.isReadable())
					{
						handleRead(key);
					}
					else
					{

					}
					if(key.isWritable())
					{
						handleWrite(key);
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
	void handleWrite(SelectionKey key) throws IOException
	{
		if(serverData.msgSent)
		{
			SocketChannel socket = serverData.getSocket.get(serverData.address);
			ByteBuffer msgBuffer = ByteBuffer.wrap(serverData.msg.getBytes());
			System.out.println(serverData.msg);
			socket.write(msgBuffer);
			msgBuffer.rewind();
			serverData.msgSent = false;
		}
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
		serverData.softUsers.add(sc);
		System.out.println("connection from " + address);
	}
	final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to the Server".getBytes());

	//
	// handle reading data into eco system
	// key.attachment() = ip + port of external user.
	//
	String type = "";
	void handleRead(SelectionKey key) throws IOException
	{
		String[] split = new String[2];
		split[0] = "";
		split[1] = "";
		SocketChannel ch = (SocketChannel) key.channel();
		buffer.clear();
		int read = 0;
		String commandtag = "/1z=";
		String command = "";
		String msg;
		StringBuilder sb = new StringBuilder();
		while((read = ch.read(buffer)) > 0)
		{
			sb = new StringBuilder();
			buffer.flip();
			byte[] bytes = new byte[buffer.limit()];
			buffer.get(bytes);
			System.out.println(sb);
			sb.append(new String(bytes));
		}
		if(read < 0) // if user disconnects
		{
			msg = key.attachment() + " left the chat. \n";
			serverData.softUsers.remove(serverData.softUsers.indexOf(ch));
			System.out.println(msg);
			ch.close();
		}
		else // if msg received from ecosystem
		{
			System.out.println("currently parsing " + sb.toString() + " from " + key.attachment());
			if(sb.toString().startsWith(commandtag))
			{
				type = sb.toString().substring(4);
				System.out.println(type);
			}
			else if(!sb.toString().startsWith(commandtag))
			{
				if(type.equals("login"))
				{
					msg = sb.toString();
					split = msg.split("=", -1);
					String username = split[0];
					String password = split[1];
					String compPassword = "";
					if(serverData.userBase.containsKey(username))
					{
						compPassword = serverData.userBase.get(username);
						if(compPassword.equals(password))
						{
							System.out.println("Password matches the Username.");
							msg("Password matches the Username.", ch);
							serverData.loggedIn.add(username);
							//serverData.onlineUsers.put(username, ch);
							System.out.println(key.attachment());
							serverData.getSocket.put(key.attachment().toString(), ch);
							//broadcast("Password matches the Username.");
						}
						else 
						{
							System.out.println("Password doesnt match Username.");	
							broadcast("Password doesnt match the Username.");
						}
					}
					else
					{
						System.out.println("Username not found.");
						broadcast("Username not found.");
					}
				}
				else if(type.equals("create"))
				{
					msg = sb.toString();
					split = msg.split("=", -1);
					String username = split[0];
					String password = split[1];
					if(serverData.userBase.containsKey(username))
					{
						System.out.println("Username already exists.");
						broadcast("Username already exists.");
					}
					else
					{
						serverData.userBase.put(username, password);
						addDatabase(username, password);
					}
				}
				else if(type.equals("msg"))
				{
					msg = sb.toString();
					if(key.attachment().toString().equals(serverData.softUsers.get(0).socket().getRemoteSocketAddress().toString()))
					{
						System.out.println("if msg");
						System.out.println(key.attachment());
						System.out.println(serverData.softUsers.get(0).socket().getRemoteSocketAddress());
						ch = serverData.softUsers.get(1);
						msg(msg, ch);
					}
					else
					{
						System.out.println("else msg");
						System.out.println(key.attachment());
						System.out.println(serverData.softUsers.get(0).socket().getRemoteSocketAddress());
						ch = serverData.softUsers.get(0);
						msg(msg, ch);
					}
					//msg = sb.toString();
					//broadcast(msg);
					//firstSingle(msg);
					//System.out.println(msg);
				}
				else
				{
					System.out.println("Type of packet not recognized.");
				}
			}
			System.out.println("type: " + type);
		}
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

	public void msg(String p_msg, SocketChannel p_ch) throws IOException
	{
		String msg = p_msg;
		ByteBuffer msgBuffer = ByteBuffer.wrap(msg.getBytes());
		SocketChannel sch = p_ch;
		sch.write(msgBuffer);
		msgBuffer.rewind();
	}
	//
	// initial mapping of serverData hashmap to database
	//
	void mapDatabase()
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
			String username = "";
			String password = "";
			int counter = 0;
			while(rs.next())
			{
				for(int i = 1; i <= columnsNumber; i++)
				{
					String columnValue = rs.getString(i);
					String columnName = rsmd.getColumnName(i);
					counter++;
					if(columnName.equals("name"))
					{
						username = columnValue;
					}
					else if(columnName.equals("password"))
					{
						password = columnValue;
					}
					if(counter == 2)
					{
						serverData.userBase.put(username, password);
						counter = 0;
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
	//
	// adding new account to database with password
	//
	void addDatabase(String p_username, String p_password)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatBoard?useSSL=false", "root", "313m3n7!");
			Statement statement = conn.createStatement();
			String query = "INSERT INTO Accounts " + "VALUES ('" + p_username + "', '" + p_password + "')"; 
			statement.executeUpdate(query);
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