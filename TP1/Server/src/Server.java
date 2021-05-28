import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
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
		 * String serverAddress = null; while (true) {
		 * System.out.print("Enter the IP address to listen on: "); BufferedReader
		 * reader = new BufferedReader(new InputStreamReader(System.in)); serverAddress
		 * = reader.readLine();
		 * 
		 * if (!isValidIPv4Address(serverAddress)) { System.out.
		 * println("The IP address you have entered is not a valid IPv4 address. Please try again. "
		 * ); continue; } else { break; } }
		 * 
		 * int serverPort = 0; while (true) {
		 * System.out.print("Enter the port number to listen on: "); BufferedReader
		 * reader = new BufferedReader(new InputStreamReader(System.in)); serverPort =
		 * Integer.parseInt(reader.readLine());
		 * 
		 * if (!isValidPort(serverPort)) { System.out.println(
		 * "The port number you have entered is not a valid (must be between 5000 and 5050). Please try again. "
		 * ); continue; } else { break; } }
		 */

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
				DataInputStream in = new DataInputStream(socket.getInputStream());
				DataOutputStream out = new DataOutputStream(socket.getOutputStream());

				String inputCommand = null;
				while ((inputCommand = in.readUTF()) != null) {
					System.out.println("Got command: " + inputCommand);

					if (inputCommand.equals("exit"))
						break;

					handleCommand(inputCommand, in, out);
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

		public void handleCommand(String command, DataInputStream inputStream, DataOutputStream outputStream)
				throws IOException {
			if (command.equals("ls")) {
				File folder = new File(this.currentPath.toString());
				File[] files = folder.listFiles();

				for (int i = 0; i < files.length; i++) {
					if (files[i].isFile()) {
						outputStream.writeUTF("[File] " + files[i].getName());
					} else if (files[i].isDirectory()) {
						outputStream.writeUTF("[Folder] " + files[i].getName());
					}
				}
			} else if (command.startsWith("cd")) {
				String[] parts = command.split(" ", 2);
				String directoryStr = parts[1];

				if (directoryStr.equals("..")) {
					Path parentDirectory = this.currentPath.getParent();
					if (parentDirectory != null)
						this.currentPath = parentDirectory;
				} else {
					Path directory = Paths.get(directoryStr);
					Path combinedPath = this.currentPath.resolve(directory);
					if (combinedPath.toFile().isDirectory())
						this.currentPath = combinedPath;
					else
						outputStream.writeUTF("\"" + combinedPath + "\"" + " is not a valid folder.");
				}

				outputStream.writeUTF("Vous êtes dans le dossier " + directoryStr + ".");

			} else if (command.startsWith("mkdir")) {
				String[] parts = command.split(" ", 2);
				String directoryStr = parts[1];
				if (!directoryStr.equals("..") && !directoryStr.equals(".")) {
					Path directory = Paths.get(directoryStr);
					Path combinedPath = this.currentPath.resolve(directory);

					File file = combinedPath.toFile();
					if (file.isDirectory()) {
						outputStream.writeUTF("Le dossier " + directoryStr + " existe déjà.");
					} else {
						file.mkdirs();
						outputStream.writeUTF("Le dossier " + directoryStr + " a été créé.");
					}
				}
			} else if (command.startsWith("upload")) {
				String fileName = inputStream.readUTF();

				// Read the file size
				byte[] fileSizeArray = new byte[4];
				inputStream.read(fileSizeArray);

				// Convert to an integer
				int fileSize = ByteBuffer.wrap(fileSizeArray).asIntBuffer().get();

				// Create a FileOutputStream and read chunks of 8192 bytes from the socket.
				Path filePath = this.currentPath.resolve(Paths.get(fileName));
				FileOutputStream fos = new FileOutputStream(filePath.toFile());

				byte[] buffer = new byte[8192];
				int bytesToRead = fileSize;
				while (bytesToRead > 0) {
					int read = inputStream.read(buffer);
					fos.write(buffer, 0, read);
					bytesToRead -= read;
				}

				fos.flush();
				fos.close();
			} else if (command.startsWith("download")) {

			}

			outputStream.writeUTF("---end---");
		}
	}
}
