import java.io.IOException;

public class Server
{
	public static void main(String[] args) throws IOException
	{
		Thread initMasterThread = new Thread();
		Thread loginMasterThread = new Thread();
		Thread instanceMasterThread = new Thread();
		Thread softLayerFilterThread = new Thread();
		
		ServerInitMaster initMaster = new ServerInitMaster(49152);
		initMasterThread = new Thread(initMaster.listen);
		initMasterThread.start();

		ServerSoftLayerFilter softLayerFilter = new ServerSoftLayerFilter();
		softLayerFilterThread = new Thread(softLayerFilter.listen);
		softLayerFilterThread.start();

		//serverThread = new Thread(loginListener.listen);
	}
}