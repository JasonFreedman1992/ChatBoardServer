import java.io.IOException;
import java.io.*;

public class Server
{
	public static void main(String[] args) throws IOException
	{
		Thread initMasterThread = new Thread();
		Thread loginMasterThread = new Thread();
		Thread instanceMasterThread = new Thread();
		ServerInitMaster initMaster = new ServerInitMaster(49152);
		initMasterThread = new Thread(initMaster.listen);
		//initMasterThread.start();
		//ServerLoginMaster loginListener = new ServerLoginMaster(49152);
		//serverThread = new Thread(loginListener.listen);
	}
}