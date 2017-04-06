import java.io.IOException;
import java.util.*;
import java.nio.*;
import java.nio.channels.*;
import java.net.*;

public class listen implements Runnable
{
	public int port = 49152;
	public ServerData serverData = new ServerData();
	public ServerSocketChannel initChannellisten = null;
	public Selector selector;
	public ByteBuffer buffer = ByteBuffer.allocate(256);

	public listen() throws IOException
	{
		initChannellisten = ServerSocketChannel.open();
		initChannellisten.socket().bind(new InetSocketAddress(port));
		initChannellisten.configureBlocking(false);
		selector = Selector.open();
		initChannellisten.register(selector, SelectionKey.OP_ACCEPT);
	}


	public void run()
	{
		try
		{
			Iterator<SelectionKey> iter;
			SelectionKey key;
			while(initChannellisten.isOpen())
			{
				selector.select();
				iter = selector.selectedKeys().iterator();
				while(iter.hasNext())
				{
					key = iter.next();
					iter.remove();
					if(key.isAcceptable())
					{
						handleAccept(key);
					}
					if(key.isReadable())
					{

					}
				}
			}
		}
		catch(IOException e)
		{
			System.out.println(" IOException, server of port 49152 terminating, stack trace: " + e);
		}
	}

	final ByteBuffer welcomeBuf = ByteBuffer.wrap("Welcome to the Server".getBytes());
	void handleAccept(SelectionKey key) throws IOException
	{
		SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
		String address = (new StringBuilder( sc.socket().getInetAddress().toString() )).append(":").append( sc.socket().getPort() ).toString();
		sc.configureBlocking(false);
		sc.register(selector, SelectionKey.OP_READ, address);
		sc.write(welcomeBuf);
		welcomeBuf.rewind();
		System.out.println("connection from " + address);
	}

	void handleRead(SelectionKey key) throws IOException
	{
		SocketChannel ch = (SocketChannel) key.channel();
		StringBuilder sb = new StringBuilder();
		buffer.clear();
		int read = 0;
		while((read = ch.read(buffer)) > 0)
		{
			buffer.flip();
			byte[] bytes = new byte[buffer.limit()];
			buffer.get(bytes);
			sb.append(new String(bytes));
			buffer.clear();
		}
		String msg;
		if(read < 0)
		{
			msg = key.attachment() + " left the chat. \n";
			ch.close();
		}
		else
		{
			msg = key.attachment() + ": " + sb.toString();
		}

		System.out.println(msg);
		broadcast(msg);
	}

	void broadcast(String msg) throws IOException
	{
		ByteBuffer msgBuffer = ByteBuffer.wrap(msg.getBytes());
		for(SelectionKey key : selector.keys())
		{
			if(key.isValid() && key.channel() instanceof SocketChannel)
			{
				SocketChannel sch = (SocketChannel) key.channel();
				sch.write(msgBuffer);
				msgBuffer.rewind();
			}
		}
	}
}