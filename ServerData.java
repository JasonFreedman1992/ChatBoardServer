import java.nio.channels.*;
import java.util.LinkedList;
import java.util.HashMap;

public class ServerData
{
	//
	// users only connected by ip and not user/password in softUsers
	//
	public static boolean msgSent = false;
	public static String msg = "";
	public static String address = "";
	public static LinkedList<SocketChannel> softUsers = new LinkedList<SocketChannel>();
	public static HashMap<String, String> userBase = new HashMap<String, String>();
	public static HashMap<String, SocketChannel> getSocket = new HashMap<String, SocketChannel>();
	public static LinkedList<User> onlineUsers = new LinkedList<User>();
	public static LinkedList<Instance> instances = new LinkedList<Instance>();
	public static int instanceTop = 0;
}