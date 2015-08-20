package doubtServer;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class ClientHandler implements Observer, Runnable {

	Socket client;
	BufferedReader in;
    DataOutputStream out;
    Broadcaster broadcaster;
	
	public ClientHandler(Socket clientSocket, Broadcaster broadcaster) throws IOException {
		client = clientSocket;
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		out = new DataOutputStream(clientSocket.getOutputStream());
		this.broadcaster = broadcaster;
		broadcaster.addObserver(this);
	}
	
	@Override
	public void update(Observable o, Object arg) {
		if (o == broadcaster && arg instanceof String) {
			try {
				out.writeBytes((String)arg);
			} catch (IOException e) {
				broadcaster.deleteObserver(this);
			}
		}
	}

	@Override
	public void run() {
		while (!client.isClosed()) {
			try {
				String input = in.readLine();
				broadcaster.broadcastMessage(input);
			} catch(Exception e) {
				try {
					client.close();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				broadcaster.deleteObserver(this);
			}
		}
	}
}
