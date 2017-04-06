import java.net.*;
import java.io.*;

public class ServerSoftLayerFilter
{
	public ServerData serverData = new ServerData();
	public DataOutputStream streamOut = null;
	public listen listen = new listen();

	public ServerSoftLayerFilter() throws IOException
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
					Thread.sleep(5000);
					if(serverData.softLogins.isEmpty())
					{
						Thread.sleep(1);
					}
					else if(!serverData.softLogins.isEmpty())
					{
						for(int i = 0; i < serverData.softLogins.size(); i++)
						{
							try
							{
								streamOut = new DataOutputStream(serverData.softLogins.get(i).getOutputStream());
								streamOut.writeUTF("packet from jasons server");
								streamOut.flush();
							}
							catch(IOException e)
							{
								serverData.softLogins.remove(i);
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
}
