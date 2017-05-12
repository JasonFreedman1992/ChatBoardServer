public class Board
{
	public boolean firstLeave = false;
	public int top = 0;
	java.util.LinkedList<User> users = new java.util.LinkedList<User>();
	boolean pub;
	boolean full;
	String name;
	String password;
	String userAdmin;
	String ID = "";
	public Board(String p_id, String p_name, boolean p_pub, String p_password)
	{
		ID = p_id;
		name = p_name;
		pub = p_pub;
		password = p_password;
	}

	public void addUser(User p_user)
	{
		users.add(p_user);
		top++;
	}
}