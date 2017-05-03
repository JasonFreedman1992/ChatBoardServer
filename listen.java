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
	public ByteBuffer buffer = ByteBuffer.allocate(51200);
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
			buffer.rewind();
			sb.append(new String(bytes));
		}
		if(read < 0) // if user disconnects, also filters people out of boards, getsockets hashmap
		{
			msg = key.attachment() + " left the server.\n";
			serverData.softUsers.remove(serverData.softUsers.indexOf(ch));
			ch.close();
			serverData.getSocket.remove(key.attachment().toString());
			try
			{
				for(int j = 0; j < serverData.Boards.size(); j++)
				{
					for(int x = 0; x < serverData.Boards.get(j).users.size(); x++)
					{
						if(key.attachment().toString().equals(serverData.Boards.get(j).users.get(x).address))
						{
							serverData.Boards.get(j).users.remove(x);
						}
					}
				}
				for(int i = 0; i < serverData.onlineUsers.size(); i++)
				{
					if(key.attachment().toString().equals(serverData.onlineUsers.get(i).address))
					{
						serverData.onlineUsers.remove(i);
					}
				}
			}
			catch(Exception e)
			{

			}
		}
		else // if msg received from ecosystem
		{
			msg = sb.toString();
			System.out.println("Parsing " + msg);
			if(msg.startsWith(commandtag))
			{
				type = msg.substring(4);
				//System.out.println(type);
				if(type.startsWith("img"))
				{	
					msg = type.substring(3);
					int i = Character.getNumericValue(msg.charAt(0));
					//System.out.println(i);
					msg = msg.substring(1);

					for(int x = 0; x < serverData.Boards.get(i).users.size(); x++)
					{
						if(!serverData.Boards.get(i).users.get(x).address.equals(key.attachment().toString()))
						{
							StringBuilder s = new StringBuilder();
							s.append(serverData.imgCommand);
							s.append(serverData.ipToUsername.get(key.attachment().toString()));
							s.append(msg);
							String s1 = s.toString();
							msg(s1, serverData.Boards.get(i).users.get(x).socket);
							//buffer.rewind();
						}
					}
				}
				else if(type.startsWith("msg"))
				{
					msg = type.substring(3);
					//System.out.println(msg);
					int i = Character.getNumericValue(msg.charAt(0));
					//System.out.println(i);
					msg = msg.substring(1);
					for(int x = 0; x < serverData.Boards.get(i).users.size(); x++)
					{
						StringBuilder s = new StringBuilder();
						s.append(serverData.msgCommand);
						s.append(serverData.ipToUsername.get(key.attachment().toString()));
						s.append(msg);
						String s0 = s.toString();
						msg(s0, serverData.Boards.get(i).users.get(x).socket);
					}
				}
				else if(type.startsWith("jbrd"))
				{
					msg = type.substring(4);
					//System.out.println(msg);
					if(serverData.Boards.isEmpty())
					{
						String reply = "Board Not Found";
						StringBuilder s = new StringBuilder();
						s.append(serverData.responseCommand);
						s.append(reply);
						String s1 = s.toString();
						msg(s1, ch);
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
										// found board
										StringBuilder s = new StringBuilder();
										s.append(serverData.responseCommand);
										s.append("Board Found");
										String s0 = s.toString();
										msg(s0, ch);
										serverData.Boards.get(i).addUser(serverData.onlineUsers.get(j));
										//
										// send board info
										//
										StringBuilder s1 = new StringBuilder();
										StringBuilder s3 = new StringBuilder();
										s3.append(serverData.responseCommand);
										s3.append("$i");
										s3.append(serverData.Boards.get(i).name);
										s3.append("=/");
										String s4 = serverData.Boards.get(i).ID;
										s3.append(s4);
										s4 = s3.toString();
										try
										{
											Thread.sleep(100);
											msg(s4, ch);
										}
										catch(InterruptedException f)
										{

										}
										//
										// refresh board members list
										//
										s1.append(serverData.responseCommand);
										s1.append("$f");
										for(int x = 0; x < serverData.Boards.get(i).users.size(); x++)
										{
											s1.append("=/");
											s1.append(serverData.Boards.get(i).users.get(x).username);

										}
										String s2 = s1.toString();
										try
										{
											for(int x = 0; x < serverData.Boards.get(i).users.size(); x++)
											{
												Thread.sleep(100);
												msg(s2, serverData.Boards.get(i).users.get(x).socket);
											}
										}
										catch(InterruptedException f)
										{

										}
									}
									else
									{

									}
								}
							}
							else
							{
								// board not found
							}
						}
					}
				}
				else if(type.startsWith("quit"))
				{
					msg = type.substring(4);
					int i = Character.getNumericValue(msg.charAt(0));
					for(int j = 0; j < serverData.Boards.get(i).users.size(); j++)
					{
						if(key.attachment().toString().equals(serverData.Boards.get(i).users.get(j).address))
						{
							serverData.Boards.get(i).users.remove(j);
						}
					}
					//
					//
					//
					StringBuilder s1 = new StringBuilder();
					s1.append(serverData.responseCommand);
					s1.append("$f");
					for(int x = 0; x < serverData.Boards.get(i).users.size(); x++)
					{
						s1.append("=/");
						s1.append(serverData.Boards.get(i).users.get(x).username);

					}
					String s2 = s1.toString();
					try
					{
						for(int x = 0; x < serverData.Boards.get(i).users.size(); x++)
						{
							Thread.sleep(100);
							msg(s2, serverData.Boards.get(i).users.get(x).socket);
						}
					}
					catch(InterruptedException f)
					{

					}
				}
				else if(type.startsWith("cbrd"))
				{
					msg = type.substring(4);
					//System.out.println(msg);
					if(serverData.Boards.isEmpty())
					{
						serverData.Boards.add(new Board(String.valueOf(serverData.boardTop), msg));
						serverData.boardTop++;
					}
					else
					{
						serverData.Boards.add(new Board(String.valueOf(serverData.boardTop), msg));
						serverData.boardTop++;
					}

				}
				else if(type.startsWith("login"))
				{
					msg = type.substring(5);
					
					split = msg.split("=", -1);
					String username = split[0];
					String password = split[1];
					String compPassword = "";
					//System.out.println(msg);
					if(serverData.userBase.containsKey(username))
					{
						compPassword = serverData.userBase.get(username);
						if(compPassword.equals(password))
						{
							// build complete message
							StringBuilder s = new StringBuilder();
							s.append(serverData.responseCommand);
							s.append("Password matches the Username.");
							String s0 = s.toString();
							msg(s0, ch);
							// add user to online users and socket to channel mapping
							User user = new User(key.attachment().toString(), username, ch);
							serverData.onlineUsers.add(user);
							serverData.getSocket.put(key.attachment().toString(), ch);
							serverData.ipToUsername.put(key.attachment().toString(), username);
						}
						else 
						{
							// build complete message
							StringBuilder s = new StringBuilder();
							s.append(serverData.responseCommand);
							s.append("Password doesnt match the Username.");
							String s0 = s.toString();
							msg(s0, ch);
						}
					}
					else
					{
						StringBuilder s = new StringBuilder();
						s.append(serverData.responseCommand);
						s.append("Username not found.");
						String s0 = s.toString();
						msg(s0, ch);
					}
				}
				else if(type.startsWith("create"))
				{
					msg = type.substring(6);
					split = msg.split("=", -1);
					String username = split[0];
					String password = split[1];
					//System.out.println(msg);
					if(serverData.userBase.containsKey(username))
					{
						StringBuilder s = new StringBuilder();
						s.append(serverData.responseCommand);
						s.append("Username already exists.");
						String s0 = s.toString();
						msg(s0, ch);
					}
					else
					{
						serverData.userBase.put(username, password);
						String id = Integer.toString(serverData.clientTotal);
						addAccountDatabase(username, password, id);
						serverData.clientTotal++;
					}
				}
				else
				{

				}
			}
			else
			{
				// packet did not start with command tag
			}
		}
		buffer.clear();
	}
	
	//
	// old broadcast method
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
				//System.out.println(key);
				//System.out.println(serverData.userBase.get(key));
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
	// also adds blank list of 10 friends with each
	// user who makes an account, signed with an x
	//
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