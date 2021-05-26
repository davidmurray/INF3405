import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.regex.Pattern;

public class Client {

	private static Socket socket;

	public static void main(String[] args) throws Exception {
		System.out.println("Client start!");

		BufferedReader sysReader = new BufferedReader(new InputStreamReader(System.in));

		String serverAddr = "127.0.0.1";
		int port = 5050;
		/*
		String serverAddr = null;

		while (true) {
			System.out.print("Enter the IP address of the server: ");
			serverAddr = sysReader.readLine();

			if (!isValidIPv4Address(serverAddr)) {
				System.out.println("The IP address you have entered is not a valid IPv4 address. Please try again. ");
				continue;
			} else {
				break;
			}
		}

		int port = 0;
		while (true) {
			System.out.print("Enter the port to connect to: ");
			port = Integer.parseInt(sysReader.readLine());

			if (!isValidPort(port)) {
				System.out.println(
						"The port number you have entered is not a valid (must be between 5000 and 5050). Please try again. ");
				continue;
			} else {
				break;
			}
		}*/

		System.out.format("Connecting to server on %s:%d%n", serverAddr, port);
		socket = new Socket(serverAddr, port);
		
	    // Create input and output streams to read from and write to the server
        PrintStream out = new PrintStream(socket.getOutputStream());
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		String command = null;
		while ((command = sysReader.readLine()) != null) {
			// Send the command to the server.
			out.println(command);

			if (command.equals("exit"))
				break;
			
			// Read the server's response.
			String response = null;
			while ((response = in.readLine()) != null) {
				if (response.equals("---end---"))
					break;
				System.out.println(response);
			}
		}
		
		System.out.println("Vous avez été déconnecté avec succès."); // TODO: check exceptions

		socket.close();
	}

	private static boolean isValidIPv4Address(String IP) {
		Pattern ipPattern = Pattern
				.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
		return ipPattern.matcher(IP).matches();
	}

	private static boolean isValidPort(int port) {
		return (port >= 5000 && port <= 5050);
	}
}
