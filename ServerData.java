import java.nio.channels.*;
import java.util.LinkedList;
import java.util.HashMap;

public class ServerData
{
	public static LinkedList<SocketChannel> Q = new LinkedList<SocketChannel>();
	public static HashMap<String, String> userBase = new HashMap<String, String>();
}