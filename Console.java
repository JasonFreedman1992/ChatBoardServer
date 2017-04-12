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
				else if(command.startsWith("get="))
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
				else if(command.startsWith("send"))
				{
					String value;
					String[] value1;
					value = command.substring(4);
					value1 = value.split("/");
					serverData.msg = value1[0];
					serverData.address = value1[1];
					System.out.println(value1[0]);
					System.out.println(value1[1]);
				}
			}
			catch(InterruptedException e)
			{

			}
		}
	}
}