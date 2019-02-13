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
		//mapDatabase();
		serverData.userBase.put("caspian", "jason231");
		serverData.userBase.put("freezy", "jason231");
		serverData.idToUsername.put("0", "caspian");
		serverData.idToUsername.put("1", "freezy");
		serverData.usernameToID.put("caspian", "0");
		serverData.usernameToID.put("freezy", "1");
		ArrayList<String> list1 = new ArrayList<String>();
		list1.add("1");
		ArrayList<String> list2 = new ArrayList<String>();
		list2.add("0");
		serverData.idToFriends.put("0", list1);
		serverData.idToFriends.put("1", list2);

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
				//
				// image request
				//
				if(type.startsWith("img"))
				{	
					msg = type.substring(3);
					int i = Character.getNumericValue(msg.charAt(0));
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
					int i = Character.getNumericValue(msg.charAt(0));
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
					if(!msg.contains("=/")) // if joining public board
					{
						msg = type.substring(4);
						if(serverData.Boards.isEmpty())
						{
							respond("Board Not Found", ch);
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
											if(serverData.Boards.get(i).pub)
											{
												//
												// respond to request
												//
												respond("Board Found", ch);
												serverData.Boards.get(i).addUser(serverData.onlineUsers.get(j));
												//
												// send board info
												//
												sendBoardInfo(serverData.Boards.get(i).name, serverData.Boards.get(i).ID, ch);
												//
												// refresh board members list
												//
												sendBoardUserListRefresh(serverData.Boards.get(i).users, ch);
											}
											else
											{
												respond("Board is Private", ch);
											}

										}
									}
								}
							}
							//
							// Board not found
							// 
							if(!boardFound)
							{
								respond("Board Not Found", ch);
							}
						}
					}
					else // if joining private board
					{
						msg = type.substring(4);
						String[] split1 = new String[2];
						split1[0] = "";
						split1[1] = "";
						split1 = msg.split("=/", -1);
						String boardName = split1[0];
						String boardPass = split1[1];
						if(serverData.Boards.isEmpty())
						{
							respond("Board Not Found", ch);
						}
						else
						{
							boolean boardFound = false;
							for(int i = 0; i < serverData.Boards.size(); i++)
							{
								if(serverData.Boards.get(i).name.equals(boardName))
								{
									boardFound = true;
									for(int j = 0; j < serverData.onlineUsers.size(); j++)
									{
										if(serverData.onlineUsers.get(j).address.equals(key.attachment().toString()))
										{	
											// found board
											// add user to board
											//
											if(!serverData.Boards.get(i).pub && serverData.Boards.get(i).password.equals(boardPass))
											{
												//
												// respond to request
												//
												respond("Board Found", ch);
												serverData.Boards.get(i).addUser(serverData.onlineUsers.get(j));
												//
												// send board info
												//
												sendBoardInfo(serverData.Boards.get(i).name, serverData.Boards.get(i).ID, ch);
												//
												// refresh board members list
												//
												sendBoardUserListRefresh(serverData.Boards.get(i).users, ch);
											}
											else if(!serverData.Boards.get(i).pub && !serverData.Boards.get(i).password.equals(boardPass))
											{
												// password doesnt match
												respond("Wrong Password", ch);

											}
											else if(serverData.Boards.get(i).pub)// board with this name is public
											{
												//
												// respond to request
												//
												respond("Board Found", ch);
												serverData.Boards.get(i).addUser(serverData.onlineUsers.get(j));
												//
												// send board info
												//
												sendBoardInfo(serverData.Boards.get(i).name, serverData.Boards.get(i).ID, ch);
												//
												// refresh board members list
												//
												sendBoardUserListRefresh(serverData.Boards.get(i).users, ch);
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
								respond("Board Not Found", ch);
							}
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
					sendBoardUserListRefresh(serverData.Boards.get(i).users, ch);			
				}
				//
				// create board request
				//
				else if(type.startsWith("cbrd"))
				{
					if(!type.contains("=/")) // if create board request is public
					{
						msg = type.substring(4);
						if(serverData.Boards.isEmpty())
						{
							serverData.Boards.add(new Board(String.valueOf(serverData.boardTop), msg, true, ""));
							serverData.boardTop++;
						}
						else
						{
							serverData.Boards.add(new Board(String.valueOf(serverData.boardTop), msg, true, ""));
							serverData.boardTop++;
						}
						sendBoardOnlineNotification(msg);
					}
					else // if create board request is private
					{
						msg = type.substring(4);
						String[] split1 = new String[2];
						split1[0] = "";
						split1[1] = "";
						split1 = msg.split("=/", -1);
						String boardName = split1[0];
						String boardPass = split1[1];
						if(serverData.Boards.isEmpty())
						{
							serverData.Boards.add(new Board(String.valueOf(serverData.boardTop), boardName, false, boardPass));
							serverData.boardTop++;
						}
						else 
						{
							serverData.Boards.add(new Board(String.valueOf(serverData.boardTop), boardName, false, boardPass));
							serverData.boardTop++;
						}
						sendBoardOnlineNotification(boardName);
					}
				}
				//
				// login account request
				//
				else if(type.startsWith("login"))
				{
					msg = type.substring(5);	
					split = msg.split("=", -1);
					String username = split[0];
					String password = split[1];
					String compPassword = "";
					if(serverData.userBase.containsKey(username))
					{
						if(!serverData.onlineUsers.contains(username))
						{
							compPassword = serverData.userBase.get(username);
							if(compPassword.equals(password))
							{
								// successfull login
								respond("Password matches the Username.", ch);
								// add user to online users and socket to channel mapping
								// map to hashmap converters
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
								respond("Password doesnt match the Username.", ch);
							}
						}
						else
						{
							// error response: User Online
							respond("User Online", ch);
						}
					}
					else
					{
						// error response: Username not Found
						respond("Username not found.", ch);
					}
				}
				//
				// create account request
				//
				else if(type.startsWith("create"))
				{
					msg = type.substring(6);
					split = msg.split("=", -1);
					String username = split[0];
					String password = split[1];
					if(serverData.userBase.containsKey(username))
					{
						respond("Username already exists.", ch);
					}
					else
					{
						serverData.userBase.put(username, password);
						String id = Integer.toString(serverData.clientTotal);
						//addAccountDatabase(username, password, id);
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
						//addFriendDatabase(idOwner, idFriend);
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
					//subFriendDatabase(idOwner, idFriend);
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
			}
		}
		buffer.clear();
	}
	
	//
	// old broadcast method
	//
	// void broadcast(String msg) throws IOException
	// {
	// 	ByteBuffer msgBuffer = ByteBuffer.wrap(msg.getBytes());
	// 	for(SelectionKey key : selector.keys())
	// 	{
	// 		if(key.isValid() && key.channel() instanceof SocketChannel)
	// 		{
	// 			SocketChannel sch = (SocketChannel) key.channel();
	// 			sch.write(msgBuffer);
	// 			msgBuffer.rewind();
	// 		}
	// 	}
	// }
	public void respond(String p_msg, SocketChannel p_ch) throws IOException
	{
		String response = serverData.responseCommand + p_msg;
		msg(response, p_ch);
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
	// remember to go thorugh and close all sql queries
	//
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
				//System.out.println(key);
				//System.out.println(serverData.userBase.get(key));
			}
		}
		catch(SQLException e)
		{

		}
		catch(ClassNotFoundException e)
		{

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

		}
	}

	//
	// add friend to database
	//
	void addFriendDatabase(String p_idOwner, String p_idFriend)
	{
		try
		{
			ArrayList<String> list = serverData.idToFriends.get(p_idOwner);
			list.add(p_idFriend);
			int friendListSize = list.size();
			//
			//
			// execute query
			Connection conn1 = DriverManager.getConnection("jdbc:mysql://localhost:3306/ChatBoard?useSSL=false", "root", "313m3n7!");
			Statement statement1 = conn1.createStatement();
			String query = "UPDATE Friends " + "SET id" + friendListSize + " = " + p_idFriend + " WHERE idOwner='" + p_idOwner + "'";
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
	// subtract friend from database
	//
	void subFriendDatabase(String p_idOwner, String p_idFriend)
	{
		try
		{
			ArrayList<String> list = serverData.idToFriends.get(p_idOwner);
			int oldListSize = list.size();
			int newListSize = 0;
			int position = 0;
			for(int i = 0; i < list.size(); i++)
			{
				if(list.get(i).equals(p_idFriend))
				{
					// id position + 1 because of array to sql database difference
					position = i + 1;
					serverData.idToFriends.get(p_idOwner).remove(i);
					newListSize = serverData.idToFriends.get(p_idOwner).size();
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

			for(int i = position; i <= oldListSize; i++)
			{
				if(position==oldListSize)
				{
					query = "UPDATE Friends SET id" + position + "='x' WHERE idOwner='" + p_idOwner + "'";
					statement1.execute(query);
				}
				else
				{
					String friendIdToMove = serverData.idToFriends.get(p_idOwner).get(position-1);
					query = "UPDATE Friends SET id" + position + "='" + friendIdToMove + "' WHERE idOwner='" + p_idOwner + "'";
					statement1.execute(query);
					position++;
				}
			}
		}
		catch(Exception e)
		{

		}
	}

	//
	// send list of online and offline friends to user 
	//
	void sendFriends(String p_username, SocketChannel p_ch)
	{
		try
		{
			ArrayList list = serverData.idToFriends.get(serverData.usernameToID.get(p_username));

			StringBuilder s = new StringBuilder(serverData.responseCommand + "$o");
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

			Thread.sleep(100);
			msg(friendsList,p_ch);
		}
		catch(Exception f)
		{
			f.printStackTrace();
		}
	}

	//
	// send notification to everyone who has user added that user has come online
	//
	void sendFriendOnlineNotification(String p_username)
	{
		try
		{
			StringBuilder s = new StringBuilder(serverData.responseCommand + "$l" + p_username);
			String friendOnline = s.toString();
			for(String key : serverData.userBase.keySet())
			{
				if(!key.equals(p_username))
				{
					String idCheck = serverData.usernameToID.get(key);
					ArrayList<String> list = serverData.idToFriends.get(idCheck);
					for(int j = 0; j < list.size(); j++)
					{
						if(list.get(j).equals(serverData.usernameToID.get(p_username)))
						{
							for(int x = 0; x < serverData.onlineUsers.size(); x++)
							{
								if(key.equals(serverData.onlineUsers.get(x).username))
								{
									Thread.sleep(100);
									msg(friendOnline, serverData.onlineUsers.get(x).socket);
								}
							}
						}
					}
				}
			}
		}
		catch(Exception f)
		{
			f.printStackTrace();
		}
	}

	//
	// send notification to everyone who has user added that user has gone offline
	//
	void sendFriendOfflineNotification(String p_username)
	{
		try
		{
			StringBuilder s = new StringBuilder(serverData.responseCommand + "$e" + p_username);
			String friendOffline = s.toString();
			for(String key : serverData.userBase.keySet())
			{
				if(!key.equals(p_username))
				{
					String idCheck = serverData.usernameToID.get(key);
					ArrayList<String> list = serverData.idToFriends.get(idCheck);
					for(int j = 0; j < list.size(); j++)
					{
						if(list.get(j).equals(serverData.usernameToID.get(p_username)))
						{
							for(int x = 0; x < serverData.onlineUsers.size(); x++)
							{
								if(key.equals(serverData.onlineUsers.get(x).username))
								{
									Thread.sleep(100);
									msg(friendOffline, serverData.onlineUsers.get(x).socket);
								}
							}
						}
					}
				}
			}
		}
		catch(Exception f)
		{
			f.printStackTrace();
		}
	}

	//
	// updated list of Boards send to user
	//
	void sendBoards(SocketChannel p_ch)
	{
		StringBuilder s = new StringBuilder(serverData.responseCommand + "$b");
		try
		{
			for(int i = 0; i < serverData.Boards.size(); i++)
			{
				s.append("=/");
				s.append(serverData.Boards.get(i).name);
			}
			String boardsList = s.toString();
			Thread.sleep(125);
			msg(boardsList, p_ch);

		}
		catch(Exception f)
		{
			f.printStackTrace();
		}
	}

	//
	// send new Board that comes online to users
	//
	void sendBoardOnlineNotification(String p_boardName)
	{
		String boardOnline = serverData.responseCommand + "$n" + p_boardName;
		try
		{
			for(int i = 0; i < serverData.onlineUsers.size(); i++)
			{
				Thread.sleep(100);
				msg(boardOnline, serverData.onlineUsers.get(i).socket);
			}
		}
		catch(Exception f)
		{
			f.printStackTrace();
		}
	}

	//
	// sends Board Disconnect when Board becomes empty
	//
	void sendBoardOfflineNotification(String p_boardName)
	{
		String boardOffline = serverData.responseCommand + "$x" + p_boardName;
		try
		{
			for(int i = 0; i < serverData.onlineUsers.size(); i++)
			{
				Thread.sleep(100);
				msg(boardOffline, serverData.onlineUsers.get(i).socket);
			}
		}
		catch(Exception f)
		{
			f.printStackTrace();
		}
	}

	void sendBoardUserListRefresh(LinkedList<User> p_userList, SocketChannel p_ch)
	{
		StringBuilder s = new StringBuilder(serverData.responseCommand + "$f");
	    for(int x = 0; x < p_userList.size(); x++)
	    {
	        s.append("=/");
	        s.append(p_userList.get(x).username);
	    }	
	    String response = s.toString();
	    try
	    {
	        for(int x = 0; x < p_userList.size(); x++)
	        {
	            Thread.sleep(100);
	            msg(response, p_userList.get(x).socket);
	        }
	    }
	    catch(Exception f)
	    {
	        f.printStackTrace();
	    }
	}

	void sendBoardInfo(String p_BoardName, String p_BoardID, SocketChannel p_ch)
	{
		String response = serverData.responseCommand + "$i" + p_BoardName + "=/" + p_BoardID;
		try
		{
		    Thread.sleep(100);
		    msg(response, p_ch);
		}
		catch(Exception f)
		{
		    f.printStackTrace();
		}
	}
}