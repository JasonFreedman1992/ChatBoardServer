import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import javax.swing.JOptionPane;

public class Client
{
	public static void main(String[] args) throws IOException
	{
		String serverAddress = JOptionPane.showInputDialog("Enter IP Address of a machine that is \n" + "running the date service on port 49152:");
		Socket s = new Socket(serverAddress, 49152);
		BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
		String answer = input.readLine();
		JOptionPane.showMessageDialog(null, answer);
		System.exit(0);
	}
}