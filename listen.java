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
						// for(int i = 0; i < serverData.Boards.size(); i++)
						// {
						// 	if(serverData.Boards.get(i).users.isEmpty())
						// 	{
						// 		sendBoardOfflineNotification(serverData.Boards.get(i).name);
						// 		serverData.Boards.remove(i);
						// 		serverData.boardTop--;
						// 	}	
						// }
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
			sendFriendOfflineNotification(serverData.ipToUsername.get(key.attachment()));
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
				//
				// image request
				//
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
				//
				// msg request
				//
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
				//
				// join board request
				//
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
						boolean boardFound = false;
						for(int i = 0; i < serverData.Boards.size(); i++)
						{
							if(serverData.Boards.get(i).name.equals(msg))
							{
								boardFound = true;
								for(int j = 0; j < serverData.onlineUsers.size(); j++)
								{
									if(serverData.onlineUsers.get(j).address.equals(key.attachment().toString()))
									{	
										// found board
										// add user to board
										//
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
						}
						//
						// Board not found
						// 
						if(!boardFound)
						{
							String reply = "Board Not Found";
							StringBuilder s = new StringBuilder();
							s.append(serverData.responseCommand);
							s.append(reply);
							String s1 = s.toString();
							msg(s1, ch);
						}
					}
				}
				//
				// quit request
				//
				else if(type.startsWith("quit"))
				{
					msg = type.substring(4);
					int i = Character.getNumericValue(msg.charAt(0));
					for(int j = 0; j < serverData.Boards.get(i).users.size(); j++)
					{
						if(key.attachment().toString().equals(serverData.Boards.get(i).users.get(j).address))
						{
							serverData.Boards.get(i).firstLeave = true;
							serverData.Boards.get(i).users.remove(j);
						}
					}
					//
					// make this change to only one piece of data sent
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
				//
				// create board request
				//
				else if(type.startsWith("cbrd"))
				{
					msg = type.substring(4);
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
					sendBoardOnlineNotification(msg);
				}
				//
				// login request
				//
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
						if(!serverData.onlineUsers.contains(username))
						{
							compPassword = serverData.userBase.get(username);
							if(compPassword.equals(password))
							{
								// successfull login
								StringBuilder s = new StringBuilder();
								s.append(serverData.responseCommand);
								s.append("Password matches the Username.");
								String s0 = s.toString();
								msg(s0, ch);
								// add user to online users and socket to channel mapping
								User user = new User(key.attachment().toString(), username, ch, serverData.usernameToID.get(username));
								serverData.onlineUsers.add(user);
								serverData.getSocket.put(key.attachment().toString(), ch);
								serverData.ipToUsername.put(key.attachment().toString(), username);
								serverData.usernameToIP.put(username, key.attachment().toString());
								sendFriends(username,ch);
								sendBoards(ch);
								sendFriendOnlineNotification(username);
							}
							else 
							{
								// error response: Password doesnt match
								StringBuilder s = new StringBuilder();
								s.append(serverData.responseCommand);
								s.append("Password doesnt match the Username.");
								String s0 = s.toString();
								msg(s0, ch);
							}
						}
						else
						{
							// error response: User Online
							StringBuilder s = new StringBuilder();
							s.append(serverData.responseCommand);
							s.append("User Online");
							String s0 = s.toString();
							msg(s0, ch);
						}
					}
					else
					{
						// error response: Username not Found
						StringBuilder s = new StringBuilder();
						s.append(serverData.responseCommand);
						s.append("Username not found.");
						String s0 = s.toString();
						msg(s0, ch);
					}
				}
				//
				// create request
				//
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
				else if(type.startsWith("addf"))
				{
					msg = type.substring(4);
					if(serverData.userBase.containsKey(msg))
					{
						String username = serverData.ipToUsername.get(key.attachment());
						String idOwner = serverData.usernameToID.get(username);

						String idFriend = serverData.usernameToID.get(msg);
						addFriendDatabase(idOwner, idFriend);
					}
				}
				else if(type.startsWith("addb"))
				{
					msg = type.substring(4);
					for(int i = 0; i < serverData.onlineUsers.size(); i++)
					{
						if(serverData.onlineUsers.get(i).username.equals(msg))
						{
							String ip = serverData.usernameToIP.get(msg);
							SocketChannel socket = serverData.getSocket.get(ip);
							StringBuilder s = new StringBuilder();
							s.append(serverData.responseCommand);
							s.append("$j");
							String board = serverData.ipToUsername.get(key.attachment().toString());
							String boardFrom = serverData.ipToUsername.get(key.attachment().toString());
							for(int x = 0; x < serverData.Boards.size(); x++)
							{
								for(int y = 0; y < serverData.Boards.get(x).users.size(); y++)
								{
									if(serverData.Boards.get(x).users.get(y).username.equals(board))
									{
										board = serverData.Boards.get(x).name;
									}
								}
							}
							s.append(board);
							s.append("=/");
							s.append(boardFrom);
							//s.append(serverData.Boards.get(i).name);
							msg(s.toString(), socket);
						}
					}
				}
				else if(type.startsWith("subf"))
				{
					msg = type.substring(4);
					String username = serverData.ipToUsername.get(key.attachment().toString());
					String idOwner = serverData.usernameToID.get(username);
					String idFriend = serverData.usernameToID.get(msg);
					subFriendDatabase(idOwner, idFriend);
				}
				else
				{

				}
				if(!serverData.Boards.isEmpty())
				{
					for(int i = 0; i < serverData.Boards.size(); i++)
					{
						if(serverData.Boards.get(i).users.isEmpty() && serverData.Boards.get(i).firstLeave)
						{
							sendBoardOfflineNotification(serverData.Boards.get(i).name);
							serverData.Boards.remove(i);
							serverData.boardTop--;
						}
					}	
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
			String id = "";
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
					if(columnName.equals("password"))
					{
						password = columnValue;
					}
					if(columnName.equals("id"))
					{
						id = columnValue;
					}
					if(counter == 3)
					{
						serverData.userBase.put(username, password);
						serverData.idToUsername.put(id, username);
						serverData.usernameToID.put(username, id);
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

		//
		// map the friends data
		//
		//
		// possibly use sql query RETURN IF NOT NULL
		//
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
			ArrayList<FriendList> friendList = new ArrayList<FriendList>();
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
						friendList.add(new FriendList(idOwned));
					}
					else if(!columnName.startsWith("idOwner"))
					{
						if(!columnValue.equals("x"))
						{
							idFriend = columnValue;
							friendList.get(friendList.size() - 1).list.add(idFriend);
						}
					}
				}
			}
			for(int i = 0; i < friendList.size(); i++)
			{
				serverData.idToFriends.put(friendList.get(i).idOwned, friendList.get(i).list);
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
			serverData.userBase.put(p_username, p_password);
			serverData.idToUsername.put(p_idOwner, p_username);
			serverData.usernameToID.put(p_username, p_idOwner);

			//
			// add to friends database
			//
			Connection conn1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatBoard?useSSL=false", "root", "313m3n7!");
			Statement statement1 = conn1.createStatement();
			query = "Insert INTO Friends " + "VALUES ('" + p_idOwner + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "', '" + "x" + "')";
			statement1.executeUpdate(query);
			ArrayList<String> friendList = new ArrayList<String>();
			serverData.idToFriends.put(p_idOwner, friendList);
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
	void addFriendDatabase(String p_idOwner, String p_idFriend)
	{
		try
		{
			ArrayList<String> list = serverData.idToFriends.get(p_idOwner);
			System.out.println("listsize : " + list.size());
			list.add(p_idFriend);
			for(int i = 0; i < list.size(); i++)
			{
				System.out.println("list : " + list.get(i));
			}
			System.out.println("new listsize : " + list.size());
			int friendListSize = list.size();
			//
			//
			// execute query
			Connection conn1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatBoard?useSSL=false", "root", "313m3n7!");
			Statement statement1 = conn1.createStatement();
			//String query = "Insert INTO Friends " + "VALUES ('" + p_idOwner + "', '" + "x" + "')";
			String query = "UPDATE Friends " + "SET id" + friendListSize + " = " + p_idFriend + " WHERE idOwner='" + p_idOwner + "'";
			System.out.println("query: " + query);
			statement1.executeUpdate(query);
			serverData.idToFriends.put(p_idOwner, list);
			String sendFriend = serverData.idToUsername.get(p_idOwner);
			String ip = serverData.usernameToIP.get(sendFriend);
			SocketChannel socket = serverData.getSocket.get(ip);
			sendFriends(sendFriend, socket);
		}
		catch(Exception e)
		{

		}
	}

	//
	//
	//
	void subFriendDatabase(String p_idOwner, String p_idFriend)
	{
		try
		{
			ArrayList<String> list = serverData.idToFriends.get(p_idOwner);
			int position = 0;
			for(int i = 0; i < list.size(); i++)
			{
				if(list.get(i).equals(p_idFriend))
				{
					// id position + 1 because of array to sql database difference
					position = i + 1;
					serverData.idToFriends.get(p_idOwner).remove(i);
				}
			}
			Connection conn1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatBoard?useSSL=false", "root", "313m3n7!");
			Statement statement1 = conn1.createStatement();
			String query = "UPDATE Friends SET id" + position + "='x' WHERE idOwner='" + p_idOwner + "'";
			statement1.executeUpdate(query);
			String sendFriend = serverData.idToUsername.get(p_idOwner);
			String ip = serverData.usernameToIP.get(sendFriend);
			SocketChannel socket = serverData.getSocket.get(ip);
			sendFriends(sendFriend, socket);
			if(position - 1 != list.size())
			{
				System.out.println("in position");
				int size = serverData.idToFriends.get(p_idOwner).size();
				// int position
				// slide everything down -1 from position<-->size
				Statement statement2 = conn1.createStatement();
				for(int i = position; i <= size; i++)
				{
					if(i+1 < serverData.idToFriends.get(p_idOwner).size())
					{
						query = "UPDATE Friends SET id" + i + " ='" + serverData.idToFriends.get(p_idOwner).get(i+1) + "' WHERE idOwner='" + p_idOwner + "'";
						statement1.execute(query);

					}
				}
				//String query = "UPDATE Friends SET id" +
			}

		}
		catch(Exception e)
		{

		}
	}

	//
	//
	//
	void sendFriends(String p_username, SocketChannel p_ch)
	{
		try
		{
			Thread.sleep(100);
			ArrayList list = serverData.idToFriends.get(serverData.usernameToID.get(p_username));
			//
			//
			//

			for(String key : serverData.idToFriends.keySet())
			{
				System.out.println(key);
				//System.out.println(serverData.userBase.get(key));
			}


			StringBuilder s = new StringBuilder();
			s.append(serverData.responseCommand);
			s.append("$o");
			for(int i = 0; i < list.size(); i++)
			{
				boolean foundOnline = false;
				for(int j = 0; j < serverData.onlineUsers.size(); j++)
				{
					if(list.get(i).equals(serverData.onlineUsers.get(j).id))
					{
						s.append("=/");
						s.append("1");
						s.append(serverData.idToUsername.get(list.get(i)));
						foundOnline = true;
					}
				}
				if(!foundOnline)
				{
					s.append("=/");
					s.append("0");
					s.append(serverData.idToUsername.get(list.get(i)));
				}
			}
			String friendsList = s.toString();
			msg(friendsList,p_ch);
		}
		catch(InterruptedException e)
		{

		}
		catch(IOException f)
		{

		}
	}

	//
	//
	//
	void sendFriendOnlineNotification(String p_username)
	{
		try
		{
			Thread.sleep(100);
			StringBuilder s = new StringBuilder();
			s.append(serverData.responseCommand);
			s.append("$l");
			s.append(p_username);
			String friendOnline = s.toString();
			String id = serverData.usernameToID.get(p_username);
			for(String key : serverData.userBase.keySet())
			{
				if(!key.equals(p_username))
				{
					String idCheck = serverData.usernameToID.get(key);
					ArrayList<String> list = new ArrayList<String>();
					list = serverData.idToFriends.get(idCheck);
					for(int j = 0; j < list.size(); j++)
					{
						if(list.get(j).equals(id))
						{
							for(int x = 0; x < serverData.onlineUsers.size(); x++)
							{
								if(key.equals(serverData.onlineUsers.get(x).username))
								{
									msg(friendOnline, serverData.onlineUsers.get(x).socket);
								}
							}
						}
					}
				}
			}
		}
		catch(InterruptedException e)
		{

		}
		catch(IOException f)
		{

		}
	}

	//
	//
	//
	void sendFriendOfflineNotification(String p_username)
	{
		try
		{
			Thread.sleep(100);
			StringBuilder s = new StringBuilder();
			s.append(serverData.responseCommand);
			s.append("$e");
			s.append(p_username);
			String friendOffline = s.toString();
			String id = serverData.usernameToID.get(p_username);
			for(String key : serverData.userBase.keySet())
			{
				if(!key.equals(p_username))
				{
					String idCheck = serverData.usernameToID.get(key);
					ArrayList<String> list = new ArrayList<String>();
					list = serverData.idToFriends.get(idCheck);
					for(int j = 0; j < list.size(); j++)
					{
						if(list.get(j).equals(id))
						{
							for(int x = 0; x < serverData.onlineUsers.size(); x++)
							{
								if(key.equals(serverData.onlineUsers.get(x).username))
								{
									msg(friendOffline, serverData.onlineUsers.get(x).socket);
								}
							}
						}
					}
				}
			}
		}
		catch(InterruptedException e)
		{

		}
		catch(IOException f)
		{
			
		}
	}

	//
	//
	//
	void sendBoards(SocketChannel p_ch)
	{
		try
		{
			Thread.sleep(100);
			StringBuilder s = new StringBuilder();
			s.append(serverData.responseCommand);
			s.append("$b");
			for(int i = 0; i < serverData.Boards.size(); i++)
			{
				s.append("=/");
				s.append(serverData.Boards.get(i).name);
			}
			String boardsList = s.toString();
			msg(boardsList, p_ch);
		}
		catch(InterruptedException e)
		{

		}
		catch(IOException f)
		{

		}
		// catch(IOException f)
		// {

		// }
	}

	//
	//
	//
	void sendBoardOnlineNotification(String p_boardName)
	{
		try
		{
			StringBuilder s = new StringBuilder();
			s.append(serverData.responseCommand);
			s.append("$n");
			s.append(p_boardName);
			String boardOnline = s.toString();
			for(int i = 0; i < serverData.onlineUsers.size(); i++)
			{
				Thread.sleep(100);
				msg(boardOnline, serverData.onlineUsers.get(i).socket);
			}
		}
		catch(InterruptedException f)
		{

		}
		catch(IOException e)
		{

		}
	}

	//
	//
	//
	void sendBoardOfflineNotification(String p_boardName)
	{
		try
		{
			StringBuilder s = new StringBuilder();
			s.append(serverData.responseCommand);
			s.append("$x");
			s.append(p_boardName);
			String boardOffline = s.toString();
			for(int i = 0; i < serverData.onlineUsers.size(); i++)
			{
				Thread.sleep(100);
				msg(boardOffline, serverData.onlineUsers.get(i).socket);
			}
		}
		catch(InterruptedException f)
		{

		}
		catch(IOException e)
		{

		}
	}
}