import java.util.*;

public class Console implements Runnable
{
	Scanner Console = new Scanner(System.in);
	ServerData serverData = new ServerData();

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
				String command = Console.nextLine();
				System.out.println("echo : " + command);
			}
			catch(InterruptedException e)
			{

			}
		}
	}
}