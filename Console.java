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
				if(command.startsWith("getQ="))
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
			}
			catch(InterruptedException e)
			{

			}
		}
	}
}