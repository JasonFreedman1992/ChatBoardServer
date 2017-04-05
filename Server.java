import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.io.*;


public class Server
{
	public static void main(String[] args) throws IOException
	{
		ServerSocket listener = new ServerSocket(49152);
		while(true)
		{
			Socket socket = listener.accept();
			ServerProcess process = new ServerProcess(socket);
		}
	}
}