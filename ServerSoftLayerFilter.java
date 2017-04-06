import java.io.IOException;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.net.*;

public class ServerSoftLayerFilter
{
	public listen listen = new listen();
	public ServerData serverData = new ServerData();
	public CharsetEncoder enc = Charset.forName("US-ASCII").newEncoder();

	class listen implements Runnable
	{
		public void run()
		{
			try
			{
				Thread.sleep(5000);
				if(serverData.Q.isEmpty())
				{

				}
				else if(!serverData.Q.isEmpty())
				{
					for(int i = 0; i < serverData.Q.size(); i++)
					{
						try
						{
							ServerData.Q.get(i).write(enc.encode(CharBuffer.wrap("packet from server")));
						}
						catch(CharacterCodingException e)
						{

						}
						catch(IOException f)
						{
							
						}
					}
				}
			}
			catch(InterruptedException e)
			{

			}
		}
	}
}