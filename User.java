public class User
{
	public java.nio.channels.SocketChannel socket;
	public String address;
	public String username;
	public String id;

	User(String p_address, String p_username, java.nio.channels.SocketChannel p_socket, String p_id)
	{
		address = p_address;
		username = p_username;
		socket = p_socket;
		id = p_id;
	}
}