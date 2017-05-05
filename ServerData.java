import java.nio.channels.*;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerData
{
	public final String msgCommand = "/1z=msg";
	public final String imgCommand = "/1z=img";
	public final String responseCommand = "/1z=resp";
	//
	// users only connected by ip and not user/password in softUsers
	//
	public static boolean msgSent = false;
	public static String msg = "";
	public static String address = "";
	public static LinkedList<SocketChannel> softUsers = new LinkedList<SocketChannel>();
	public static HashMap<String, String> userBase = new HashMap<String, String>();

	// enter ip get socketchannel
	public static HashMap<String, SocketChannel> getSocket = new HashMap<String, SocketChannel>();
	// enter id get list of friend id's
	public static HashMap<String, ArrayList<String>> idToFriends = new HashMap<String, ArrayList<String>>();
	//
	public static HashMap<String, String> idToUsername = new HashMap<String, String>();
	public static HashMap<String, String> usernameToID = new HashMap<String, String>();
	// list of users
	public static LinkedList<User> onlineUsers = new LinkedList<User>();
	// list of instances
	public static LinkedList<Board> Boards = new LinkedList<Board>();
	public static HashMap<String, String> ipToUsername = new HashMap<String, String>();
	public static int clientTotal = 0;
	public static int boardTop = 0;

	public static byte[] imgArray = new byte[51200];
	public static int imgSize;

	public static boolean ready = false;
}