import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class Server {

	private static ServerSocket listener;

	public static void main(String[] args) throws Exception {
		System.out.println("Server start!");

		int clientNumber = 0;
		
		String serverAddress = "127.0.0.1";
		int serverPort = 5050;
		/*
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
				System.out.println(
						"The port number you have entered is not a valid (must be between 5000 and 5050). Please try again. ");
				continue;
			} else {
				break;
			}
		}*/

		listener = new ServerSocket();
		listener.setReuseAddress(true);
		InetAddress serverIP = InetAddress.getByName(serverAddress);

		listener.bind(new InetSocketAddress(serverIP, serverPort));

		System.out.format("The server is running on %s:%d\n", serverAddress, serverPort);

		try {
			while (true) {
				new ClientHandler(listener.accept(), clientNumber++).start();
			}
		} finally {
			listener.close();
		}
	}

	private static boolean isValidIPv4Address(String IP) {
		Pattern ipPattern = Pattern
				.compile("^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
		return ipPattern.matcher(IP).matches();
	}

	private static boolean isValidPort(int port) {
		return (port >= 5000 && port <= 5050);
	}

	private static class ClientHandler extends Thread {
		private Socket socket;
		private int clientNumber;
		private Path currentPath;

		public ClientHandler(Socket socket, int clientNumber) {
			this.socket = socket;
			this.clientNumber = clientNumber;
			this.currentPath = Paths.get("").toAbsolutePath();
			System.out.println(this.currentPath);

			System.out.println("New connection with client #" + clientNumber + " at " + socket);
		}

		public void run() {
			try {
				PrintStream out = new PrintStream(socket.getOutputStream());
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				//out.writeUTF("Hello from server - you are client #" + clientNumber);
				
				String inputCommand = null;
			    while ((inputCommand = in.readLine()) != null) {
					System.out.println("Got command: " + inputCommand);
				
					if (inputCommand.equals("exit"))
						break;
					
					handleCommand(inputCommand, out);
				}

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
		
		public void handleCommand(String command, PrintStream outputStream) {
			if (command.equals("ls")) {
				File folder = new File(this.currentPath.toString());
				File[] listOfFiles = folder.listFiles();

				for (int i = 0; i < listOfFiles.length; i++) {
				  if (listOfFiles[i].isFile()) {
				    outputStream.println("[File] " + listOfFiles[i].getName());
				  } else if (listOfFiles[i].isDirectory()) {
					outputStream.println("[Folder] " + listOfFiles[i].getName());
				  }
				}
				
				outputStream.println("---end---");
			} else if (command.contains("cd")) {
				String[] parts = command.split(" ", 2);
				String directoryStr = parts[1];
				
				if (directoryStr.equals("..")) {
					Path parentDirectory = this.currentPath.getParent();
					if (parentDirectory != null)
						this.currentPath = parentDirectory;
				} else {
					Path directory = Paths.get(directoryStr);
					Path combinedDirectory = this.currentPath.resolve(directory);
					if (combinedDirectory.toFile().isDirectory())
						this.currentPath = combinedDirectory;
					else
						outputStream.println("\"" + combinedDirectory + "\"" + " is not a valid folder.");
				}
				
				System.out.println(this.currentPath);

				outputStream.println("---end---");
			} else if (command.equals("mkdir")) {
				
			} else if (command.contains("upload")) {
				
			} else if (command.contains("download")) {
				
			}

		}
	}

}
