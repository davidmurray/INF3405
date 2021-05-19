import java.io.DataInputStream;
import java.net.Socket;


public class Client {

	private static Socket socket;
	
	public static void main(String[] args) throws Exception {
		System.out.println("Client start!");
		
		String serverAddr = "127.0.0.1";
		int port = 5002;
		
		socket = new Socket(serverAddr, port);
		System.out.format("The server is running on %s:%d", serverAddr, port);
		
		DataInputStream in = new DataInputStream(socket.getInputStream());
		
		String helloMessage = in.readUTF();
		System.out.println(helloMessage);
		
		socket.close();
	}

}
