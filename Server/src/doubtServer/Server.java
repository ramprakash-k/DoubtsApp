package doubtServer;

import java.io.*;
import java.net.*;

public class Server {

	public static void main(String[] args) {

		if (args.length != 1) {
			System.err.println("Usage : java doubtServer/Server <port number>");
			System.exit(1);
		}
		int portNumber = Integer.parseInt(args[0]);
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		final Broadcaster broadcaster = new Broadcaster();
		System.out.println("Server started at port number "+Integer.toString(portNumber));
		while (true) {
			try {
				Socket clientSocket = serverSocket.accept();
				System.out.println(clientSocket.getInetAddress().getHostAddress() + " connected.");
				new Thread(new ClientHandler(clientSocket,broadcaster)).start();
			} catch(IOException e) {
				try {
					serverSocket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
				break;
			}
		}
	}
}
