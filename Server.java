import java.io.IOException;

public class Server
{
	public static void main(String[] args) throws IOException
	{
		Thread initMasterThread = new Thread();
		Thread loginMasterThread = new Thread();
		Thread instanceMasterThread = new Thread();
		Thread softLayerFilterThread = new Thread();
		
		initMasterThread = new Thread(new listen());
		initMasterThread.start();

		//serverThread = new Thread(loginListener.listen);
	}
}