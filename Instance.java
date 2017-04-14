public class Instance
{
	public int top = 0;
	java.util.LinkedList<User> users = new java.util.LinkedList<User>();
	boolean pub;
	boolean full;
	String name;
	int ID;
	public Instance(int p_id)
	{
		ID = p_id;
	}

	public void addUser(User p_user)
	{
		users.add(p_user);
		top++;
	}
}