public class Board
{
	public int top = 0;
	java.util.LinkedList<User> users = new java.util.LinkedList<User>();
	boolean pub;
	boolean full;
	String name;
	String password;
	String userAdmin;
	int ID;
	public Board(int p_id, String p_name)
	{
		ID = p_id;
		name = p_name;
	}

	public void addUser(User p_user)
	{
		users.add(p_user);
		top++;
	}
}