import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class Client {

	private static Socket socket;

	public static void main(String[] args) throws Exception {
		BufferedReader sysReader = new BufferedReader(new InputStreamReader(System.in));

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
		}

		System.out.format("Connecting to server on %s:%d...", serverAddr, port);
		socket = new Socket(serverAddr, port);
		
		while (socket.isConnected() == false) { ;; }
		System.out.println("connected!");
		
	    // Create input and output streams to read from and write to the server
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		DataInputStream in = new DataInputStream(socket.getInputStream());

		String command = null;
		while ((command = sysReader.readLine()) != null) {
			// The "upload" and "download" commands are special so we handle them differently.
			if (command.startsWith("upload")) {
				String fileName = command.split(" ", 2)[1];
				if (sendFileWithName(fileName, out) == false)
					continue;
			} else if (command.startsWith("download")) {
				String fileName = command.split(" ", 2)[1];
				receiveFileWithName(fileName, in, out);
			} else {
				// Send the command to the server.
				out.writeUTF(command);

				if (command.equals("exit"))
					break;				
			}
			
			// Read the server's response until we see "---end---".
			String response = null;
			while ((response = in.readUTF()) != null) {
				if (response.equals("---end---"))
					break;
				System.out.println(response);
			}
		}

		socket.close();
		System.out.println("Vous avez été déconnecté avec succès."); // TODO: check exceptions
	}
	
	// This returns false if there was an error.
	private static boolean sendFileWithName(String fileName, DataOutputStream outputStream) throws IOException {		
		Path filePath = Paths.get(fileName);
		if (Files.exists(filePath) == false) {
			System.out.println("The file with name " + fileName + " does not exist.");
			return false;
		}

		// Load the file in memory
		byte[] data = Files.readAllBytes(filePath);
		
		// 4 bytes which represents the file size.
		byte[] fileSize = ByteBuffer.allocate(4).putInt(data.length).array();
		
		outputStream.writeUTF("upload");
		
		// Send the file name
		outputStream.writeUTF(fileName);
		
		// Send the data's size
		outputStream.write(fileSize);
		
		// Send the data to the server.
		outputStream.write(data);
		outputStream.flush();
		
		return true;
	}

	private static void receiveFileWithName(String fileName, DataInputStream inputStream, DataOutputStream outputStream) throws IOException {		
		// Send the request to the server
		outputStream.writeUTF("download");
		outputStream.writeUTF(fileName);
		
		// Read the file size received from the server
		byte[] fileSizeArray = new byte[4];
		inputStream.read(fileSizeArray);

		// Convert to an integer
		int length = ByteBuffer.wrap(fileSizeArray).asIntBuffer().get();

		// Create a FileOutputStream and read chunks of 8192 bytes from the socket.
		Path filePath = Paths.get(fileName);
		FileOutputStream fos = new FileOutputStream(filePath.toFile());

		byte[] buffer = new byte[8192];
		int bytesToRead = length;
		while (bytesToRead > 0) {
			// Read 8192 bytes or less than that if there is fewer than that left to read.
			int min = Math.min(bytesToRead, buffer.length);
			int read = inputStream.read(buffer, 0, min);
			fos.write(buffer, 0, read);
			bytesToRead -= read;
		}

		fos.flush();
		fos.close();
		System.out.println("Finished receviing feverything");
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
