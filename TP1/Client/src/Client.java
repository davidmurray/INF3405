import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Pattern;

public class Client {

	private static Socket socket;

	public static void main(String[] args) throws Exception {
		System.out.println("Client start!");

		String serverAddr = null;

		while (true) {
			System.out.print("Enter the IP address of the server: ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			serverAddr = reader.readLine();

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
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			port = Integer.parseInt(reader.readLine());

			if (!isValidPort(port)) {
				System.out.println(
						"The port number you have entered is not a valid (must be between 5000 and 5050). Please try again. ");
				continue;
			} else {
				break;
			}
		}

		socket = new Socket(serverAddr, port);
		System.out.format("The server is running on %s:%d", serverAddr, port);

		DataInputStream in = new DataInputStream(socket.getInputStream());

		String helloMessage = in.readUTF();
		System.out.println(helloMessage);

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
