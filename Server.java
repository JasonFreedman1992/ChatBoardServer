import java.io.IOException;
import java.io.*;

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

		//ServerLoginMaster loginMaster = new ServerLoginMaster();
		//loginMasterThread = new Thread(loginMaster.listen);
		//loginMasterThread.start();

		ServerSoftLayerFilter softlayer = new ServerSoftLayerFilter();
		softLayerFilterThread = new Thread(softlayer.listen);
		softLayerFilterThread.start();
		//serverThread = new Thread(loginListener.listen);
	}
}