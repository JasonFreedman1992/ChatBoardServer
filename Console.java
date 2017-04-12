import java.util.*;

public class Console implements Runnable
{
	Scanner Console = new Scanner(System.in);
	ServerData serverData = new ServerData();
	String[] split = new String[2];
	Console()
	{

	}

	public void run()
	{
		while(true)
		{
			try
			{
				Thread.sleep(1000);
				split[0] = "";
				split[1] = "";
				String command = Console.nextLine();
				//System.out.println("echo : " + command);
				if(command.startsWith("getuser="))
				{
					String value;
					split = command.split("=");
					value = split[1];
					//
					System.out.println("Password for " + value + ": " + serverData.userBase.get(value));
				}
				else if(command.startsWith("get=su"))
				{
					if(serverData.softUsers.isEmpty())
					{

					}
					else
					{
						for(int i = 0; i < serverData.softUsers.size(); i++)
						{
							System.out.println(serverData.softUsers.get(i));
						}
					}
				}
				else if(command.startsWith("get=gs"))
				{
					if(serverData.getSocket.isEmpty())
					{

					}
					else
					{
						for(String key : serverData.getSocket.keySet())
						{
							System.out.println(key);
							System.out.println(serverData.getSocket.get(key).toString());
						}
					}
				}
				else if(command.startsWith("send"))
				{
					String value;
					String[] value1;
					value = command.substring(4);
					value1 = value.split("=", -1);
					serverData.msg = value1[0];
					serverData.address = value1[1];
					serverData.msgSent = true;
				}
				else if(command.startsWith("get=ou"))
				{
					if(serverData.onlineUsers.isEmpty())
					{

					}
					else
					{
						for(int i = 0; i < serverData.onlineUsers.size(); i++)
						{
							System.out.println(serverData.onlineUsers.get(i).username);
							System.out.println(serverData.onlineUsers.get(i).address);
							System.out.println(serverData.onlineUsers.get(i).socket);
						}
					}
				}
				else if(command.startsWith("get=in"))
				{
					if(serverData.instances.isEmpty())
					{

					}
					else
					{
						for(int i = 0; i < serverData.instances.size(); i++)
						{
							System.out.println("instance ID: " + serverData.instances.get(i).ID);
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