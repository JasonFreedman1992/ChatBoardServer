import java.io.IOException;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.sql.*;
import javax.swing.JOptionPane;
import java.util.Arrays;


public class listen implements Runnable
{
	public int port = 49152;
	public ServerData serverData = new ServerData();
	public ServerSocketChannel initChannellisten;
	public Selector selector = null;
	public ByteBuffer buffer = ByteBuffer.allocate(256);
	String type = "";
	final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to the Server".getBytes());
	StringBuilder imgbytes;
	byte[] bytes;

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
	SelectionKey key;
	public void run()
	{
		mapDatabase();
		while(initChannellisten.isOpen())
		{
			try
			{
				Iterator<SelectionKey> iter;
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
						System.out.println("Cancelled Key!, key.isWritable() exception!");
					}					

				}
			}
			catch(IOException e)
			{
				System.out.println(" IOException, " + key.attachment() + " terminating, stack trace: " + e);
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
			bytes = new byte[buffer.limit()];
			buffer.get(bytes);
			sb.append(new String(bytes));
		}
		if(read < 0) // if user disconnects
		{
			msg = key.attachment() + " left the server.\n";
			serverData.softUsers.remove(serverData.softUsers.indexOf(ch));
			ch.close();
			serverData.getSocket.remove(key.attachment().toString());
			try
			{
				for(int i = 0; i < serverData.Boards.size(); i++)
				{
					for(int j = 0; j < serverData.Boards.get(i).top; j++)
					{
						if(serverData.Boards.get(i).users.get(j).address.equals(key.attachment().toString()))
						{
							serverData.Boards.get(i).users.remove(j);
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
			//System.out.println(sb.length());
			//System.out.println("currently parsing " + sb.toString() + " from " + key.attachment());
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
						String id = Integer.toString(serverData.clientTotal);
						addAccountDatabase(username, password, id);
						serverData.clientTotal++;
					}
				}
				else if(type.equals("msg"))
				{
					msg = sb.toString();
					System.out.println(msg);
					for(int i = 0; i < serverData.Boards.size(); i++)
					{
						for(int j = 0; j < serverData.Boards.get(i).users.size(); j++)
						{
							if(key.attachment().toString().equals(serverData.Boards.get(i).users.get(j).address))
							{
								for(int x = 0; x < serverData.Boards.get(i).users.size(); x++)
								{
									msg(msg, serverData.Boards.get(i).users.get(x).socket);
								}
							}
						}
					}
				}
				else if(type.equals("cbrd"))
				{
					msg = sb.toString();
					if(serverData.Boards.isEmpty())
					{
						serverData.Boards.add(new Board(0, msg));
						serverData.boardTop++;
					}
					else
					{
						serverData.Boards.add(new Board(serverData.boardTop, msg));
						serverData.boardTop++;
					}

				}
				else if(type.equals("jbrd"))
				{
					msg = sb.toString();
					if(serverData.Boards.isEmpty())
					{
						String reply = "Board Not Found";
						msg(reply, ch);
					}
					else
					{
						for(int i = 0; i < serverData.Boards.size(); i++)
						{
							if(serverData.Boards.get(i).name.equals(msg))
							{
								for(int j = 0; j < serverData.onlineUsers.size(); j++)
								{
									if(serverData.onlineUsers.get(j).address.equals(key.attachment().toString()))
									{
										serverData.Boards.get(i).addUser(serverData.onlineUsers.get(j));
										msg("Board Found", ch);
									}
									else
									{

									}
								}
							}
							else
							{
								// board not found
								System.out.println("Board not found");
							}
						}
					}
					type = "";
				}
				else if(type.equals("img"))
				{
					System.out.println(bytes);
					//System.out.println("something");
				}
				else
				{
					//System.out.println("Type of packet not recognized.");
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
						idOwned = columnValue;
						list.add(new FriendList(idOwned));
					}
					else if(!columnName.startsWith("idOwner"))
					{
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
			String query = "INSERT INTO Accounts " + "VALUES ('" + p_username + "', '" + p_password + "', '" + p_idOwner + "')"; 
			Statement statement = conn.createStatement();
			statement.executeUpdate(query);

			Connection conn1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatBoard?useSSL=false", "root", "313m3n7!");
			Statement statement1 = conn1.createStatement();
			query = "Insert INTO Friends " + "VALUES ('" + p_idOwner + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "')";
			statement1.executeUpdate(query);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
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