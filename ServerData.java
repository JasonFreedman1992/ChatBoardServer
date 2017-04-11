import java.nio.channels.*;
import java.util.LinkedList;
import java.util.HashMap;

public class ServerData
{
	//
	// users only connected by ip and not user/password in softUsers
	//
	public static LinkedList<SocketChannel> softUsers = new LinkedList<SocketChannel>();
	public static HashMap<String, String> userBase = new HashMap<String, String>();
	public static LinkedList<User> onlineUsers = new LinkedList<User>();
}