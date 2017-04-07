import java.io.IOException;

public class Server
{
	public static void main(String[] args) throws IOException
	{
		Thread initMasterThread = new Thread();
		
		listen initMaster = new listen();
		initMasterThread = new Thread(initMaster);
		initMasterThread.start();

		//serverThread = new Thread(loginListener.listen);
	}
}