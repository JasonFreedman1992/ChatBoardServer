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
	String type = "";
	final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to the Server".getBytes());

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
					try
					{
						if(key.isWritable())
						{
							handleWrite();
						}
						else
						{

						}
					}
					catch(CancelledKeyException e)
					{
						//e.printStackTrace();
						System.out.println("Cancelled Key!, key.isWritable() exception!");
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
	void handleWrite() throws IOException
	{
		if(serverData.msgSent)
		{
			SocketChannel socket = serverData.getSocket.get(serverData.address);
			ByteBuffer msgBuffer = ByteBuffer.wrap(serverData.msg.getBytes());
			//System.out.println(serverData.msg);
			try
			{
				socket.write(msgBuffer);
			}
			catch(NullPointerException e)
			{

			}
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
		System.out.println("Connection from " + address);
	}

	//
	// handle reading data into eco system
	// key.attachment() = ip + port of external user.
	//
	void handleRead(SelectionKey key) throws IOException
	{
		// setup for a split for parsing data
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
			sb.append(new String(bytes));
		}
		if(read < 0) // if user disconnects
		{
			msg = key.attachment() + " left the server.\n";
			// remove from softUsers and getsocket list
			serverData.softUsers.remove(serverData.softUsers.indexOf(ch));
			serverData.getSocket.remove(key.attachment().toString());
			try
			{
				for(int i = 0; i < serverData.instances.size(); i++)
				{
					for(int j = 0; j < serverData.instances.get(i).top; j++)
					{
						if(serverData.instances.get(i).users.get(j).address.equals(key.attachment().toString()))
						{
							serverData.instances.get(i).users.remove(j);
						}
					}
				}
			}
			catch(Exception e)
			{

			}
			broadcast(msg);
			ch.close();
		}
		else // if msg received from ecosystem
		{
			System.out.println("currently parsing " + sb.toString() + " from " + key.attachment());
			if(sb.toString().startsWith(commandtag))
			{
				type = sb.toString().substring(4);
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
							msg("Password matches the Username.", ch);
							User user = new User(key.attachment().toString(), username, ch);
							serverData.onlineUsers.add(user);
							serverData.getSocket.put(key.attachment().toString(), ch);
							if(serverData.instances.isEmpty())
							{
								serverData.instances.add(new Instance(0));
								serverData.instanceTop++;
							}
							else if(!serverData.instances.isEmpty() && (serverData.getSocket.size() % 2 == 0))
							{
								serverData.instances.add(new Instance(serverData.instanceTop));
								serverData.instanceTop++;
							}
							else
							{

							}
							serverData.instances.get(0).addUser(user);
						}
						else 
						{
							msg("Password doesnt match the Username.", ch);
						}
					}
					else
					{
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
						broadcast("Username already exists.");
					}
					else
					{
						serverData.userBase.put(username, password);
						addAccountDatabase(username, password, Integer.toString(serverData.clientTotal+1));
						serverData.clientTotal++;
					}
				}
				else if(type.equals("msg"))
				{
					msg = sb.toString();
					System.out.println(msg);
					for(int i = 0; i < serverData.instances.size(); i++)
					{
						for(int j = 0; j < serverData.instances.get(i).users.size(); j++)
						{
							if(key.attachment().toString().equals(serverData.instances.get(i).users.get(j).address))
							{
								for(int x = 0; x < serverData.instances.get(i).users.size(); x++)
								{
									msg(msg, serverData.instances.get(i).users.get(x).socket);
								}
							}
						}
					}
				}
				else
				{
					System.out.println("Type of packet not recognized.");
				}
			}
		}
	}
	
	//
	// broadcast
	//
	void broadcast(String msg) throws IOException
	{
		System.out.println(msg);
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
	// sql mapping class
	//
	class FriendList
	{
		String idOwned = "";
		public ArrayList<String> list = new ArrayList<String>();
		FriendList(String p_idOwned)
		{
			idOwned = p_idOwned;
		}
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
						serverData.clientTotal++;
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

		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatBoard?useSSL=false", "root", "313m3n7!");
			Statement statement = conn.createStatement();
			String query = "SELECT * FROM Friends";
			ResultSet rs = statement.executeQuery(query);
			ResultSetMetaData rsmd = rs.getMetaData();
			int columnsNumber = rsmd.getColumnCount();
			String idOwned = "";
			String idFriend = "";
			ArrayList<FriendList> list = new ArrayList<FriendList>();
			int counter = 0;
			while(rs.next())
			{
				for(int i = 1; i <= columnsNumber; i++)
				{
					String columnValue = rs.getString(i);
					String columnName = rsmd.getColumnName(i);
					if(columnName.equals("idOwner"))
					{
						System.out.println("equals idowner");
						idOwned = columnValue;
						list.add(new FriendList(idOwned));
					}
					else if(!columnName.startsWith("idOwner"))
					{
						System.out.println("doesnt start with idowner");
						if(!columnValue.equals("x"))
						{
							idFriend = columnValue;
							list.get(list.size() - 1).list.add(idFriend);
						}
					}
				}
			}
			for(int i = 0; i < list.size(); i++)
			{
				serverData.idToFriends.put(list.get(i).idOwned, list.get(i).list);
			}
		}
		catch(SQLException e)
		{

		}
		catch(ClassNotFoundException e)
		{

		}
	}
	//
	// adding new account to database with password
	//
	void addAccountDatabase(String p_username, String p_password, String p_idOwner)
	{
		try
		{
			Class.forName("com.mysql.jdbc.Driver");
			Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatBoard?useSSL=false", "root", "313m3n7!");
			Statement statement = conn.createStatement();
			String query = "INSERT INTO Accounts " + "VALUES ('" + p_username + "', '" + p_password + "')"; 
			statement.executeUpdate(query);

			System.out.println("made it 429");
			Connection conn1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatBoard?useSSL=false", "root", "313m3n7!");
			Statement statement1 = conn.createStatement();
			System.out.println("made it 432");
			query = "Insert INTO Friends " + "VALUES ('" + p_idOwner + "')";
			statement1.executeUpdate(query);
			System.out.println("made it 435");
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
	//
	//
	void addFriendDatabase()
	{

	}
}