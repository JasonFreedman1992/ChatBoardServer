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
	public DataOutputStream streamOut = null;
	public DataInputStream streamIn = null;
	public Thread[] threads = new Thread[5];

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
					Thread.sleep(1000);
					//System.out.println(serverData.softLogins.size());
					if(!serverData.softLogins.isEmpty())
					{
						for(int i = 0; i < serverData.softLogins.size(); i++)
						{
							streamIn = new DataInputStream(serverData.softLogins.get(i).getInputStream());
							if(streamIn.available() > 0)
							{
								System.out.println(streamIn.available());
							}
							else
							{
								System.out.println(streamIn.available());
								System.out.println(streamIn.readUTF());
								System.out.println(streamIn.readUTF());
							}
						}
					}
					else
					{

					}
				}
				catch(Exception e)
				{

				}
			}
		}
	}
}