import java.net.*;
import java.io.*;

public class ServerSoftLayerFilter
{
	ServerData serverData = new ServerData();
	DataOutputStream streamOut = null;
	public listen listen = new listen();

	class listen implements Runnable
	{
		public void run()
		{
			while(true)
			{
				if(serverData.softLogins.isEmpty())
				{

				}
				else if(!serverData.softLogins.isEmpty())
				{
					for(int i = 0; i < serverData.softLogins.size(); i++)
					{	
						try
						{
							streamOut = new DataOutputStream(serverData.softLogins.get(i).getOutputStream());
							streamOut.writeUTF("sending packet");
							streamOut.flush();
						}
						catch(IOException e)
						{
							System.out.println(e);
							serverData.softLogins.remove(i);
						}
					}
				}
			}
		}
	}
}