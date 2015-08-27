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
				System.out.println("sending message : " + (String)arg);
				out.writeBytes((String)arg + "\n");
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
				if (input!=null) {
					System.out.println("message received : " + input);
					String info[] = input.split("[|]");
					if (info[info.length - 1].equals("I'm Out")) {
						disconnect();
					} else {
						broadcaster.broadcastMessage(input);
					}
				} else {
					System.out.println("null received");
					disconnect();
				}
			} catch(Exception e) {
				disconnect();
			}
		}
	}
	
	private void disconnect() {
		try {
			client.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		broadcaster.deleteObserver(this);
	}
}
