public class Filter implements Runnable
{
	ServerData serverData = new ServerData();
	Filter()
	{

	}

	public void run()
	{
		if(serverData.Q.isEmpty())
		{

		}
		else
		{
			for(int i = 0; i < serverData.Q.size(); i++)
			{
				if(!serverData.Q.get(i).isConnected())
				{
					serverData.Q.remove(i);
				}
			}
		}
	}
}