import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private static ServerSocket listener;

	public static void main(String[] args) throws Exception {
		System.out.println("Server start!");

		int clientNumber = 0;
		
		String serverAddress = "127.0.0.1";
		int serverPort = 5002;
		
		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);
		
		listener.bind(new InetSocketAddress(serverIP, serverPort));
		
		System.out.format("The server is running on %s:%d", serverAddress, serverPort);
		
		try {
			while (true) {
				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		} finally {
			listener.close();
		}
	}
	
	
	private static class ClientHandler extends Thread
	{
		private Socket socket;
		private int clientNumber;

		public ClientHandler(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;

			System.out.println("New connection with client #" + clientNumber + " at " + socket);
		}

		public void run() {
			try
			{
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());

				out.writeUTF("Hello from server - you are client #" + clientNumber);

			} catch (IOException e) {
				System.out.println("Error handling client #" + clientNumber + ": " + e);
			} finally {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("Could not close socket.");
				}

				System.out.println("Connection with client # " + clientNumber + " closed");
			}
		}
	}

}
