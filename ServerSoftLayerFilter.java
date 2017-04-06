import java.io.IOException;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.net.*;

public class ServerSoftLayerFilter
{
	public listen listen = new listen();
	public ServerData serverData = new ServerData();
	public Selector selector = null;
	public ByteBuffer buffer = null;

	class listen implements Runnable
	{
		public void run()
		{

		}
	}
}