import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Pattern;

public class Server {
	
	private static ServerSocket listener;

	public static void main(String[] args) throws Exception {
		System.out.println("Server start!");

		int clientNumber = 0;
		
		String serverAddress = null;
		while (true) {
    	    System.out.print("Enter the IP address to listen on: ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			serverAddress = reader.readLine();
			
			if (!isValidIPv4Address(serverAddress)) {
	    	    System.out.println("The IP address you have entered is not a valid IPv4 address. Please try again. ");
	    	    continue;
			} else {
				break;
			}
		}
		
		int serverPort = 0;
		while (true) {
    	    System.out.print("Enter the port number to listen on: ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			serverPort = Integer.parseInt(reader.readLine());
			
			if (!isValidPort(serverPort)) {
	    	    System.out.println("The port number you have entered is not a valid (must be between 5000 and 5050). Please try again. ");
	    	    continue;
			} else {
				break;
			}
		}
		
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
	
	private static boolean isValidIPv4Address(String IP) {
    	Pattern ipPattern = Pattern.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
    	return ipPattern.matcher(IP).matches();
	}
	
	private static boolean isValidPort(int port) {
		return (port >= 5000 && port <= 5050);
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
