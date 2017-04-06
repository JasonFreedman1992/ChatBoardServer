import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.*;
import java.io.*;

public class ServerLoginMaster
{
	Scanner console = new Scanner(System.in);
	public ServerData serverData = new ServerData();
	public listen listen = new listen();
	public DataInputStream streamIn = null;
	public DataOutputStream streamOut = null;

	public ServerLoginMaster() throws IOException
	{

	}

	class listen implements Runnable
	{
		public void run()
		{
			while(true)
			{
				try
				{
					if(serverData.softLogins.isEmpty())
					{
						Thread.sleep(1000);
						System.out.println("no soft logins to process");
					}
					else if(!serverData.softLogins.isEmpty())
					{
						for(int i = 0; i < serverData.softLogins.size(); i++)
						{
							streamIn = new DataInputStream(serverData.softLogins.get(i).getInputStream());
							if(!streamIn.readUTF().equals(""))
							{
								System.out.println(streamIn.readUTF());
								System.out.println(streamIn.readUTF());
							}
						}
					}
				}
				catch(Exception e)
				{
					System.out.println(e);
				}
			}
		}
	}

	class SubServer
	{
		Socket socket;
		public SubServer(Socket p_socket)
		{
			socket = p_socket;
		}
	}
}