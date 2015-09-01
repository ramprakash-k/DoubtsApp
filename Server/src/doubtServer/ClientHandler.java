package doubtServer;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

public class ClientHandler implements Observer, Runnable {
	
	Socket client;
	BufferedReader in;
    DataOutputStream out;
    Broadcaster broadcaster;
    DoubtHandler doubtHandler;
	
	public ClientHandler(Socket clientSocket, Broadcaster broadcaster, DoubtHandler handler) throws IOException {
		client = clientSocket;
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		out = new DataOutputStream(clientSocket.getOutputStream());
		this.broadcaster = broadcaster;
		doubtHandler = handler;
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
		String roll;
		try {
			String input = in.readLine();
			System.out.println("message received : " + input);
			if (input != null) {
				String[] info = input.split("[|]");
				if (!info[0].equals("I Am")) {
					out.writeBytes("Wrong app. Disconnecting.");
					System.out.println("Wrong app. Disconnecting.");
					disconnect();
				} else {
					roll = info[1];
					for(String instr : doubtHandler.genAll(roll)) {
						System.out.println("Sending instr : " + instr);
						out.writeBytes(instr + "\n");
					}
				}
			} else {
				System.out.println("null roll received");
				disconnect();
			}
		} catch(IOException e) {
			System.out.println("roll problem occured");
			e.printStackTrace();
			disconnect();
		}
		while (!client.isClosed()) {
			try {
				String input = in.readLine();
				if (input!=null) {
					System.out.println("message received : " + input);
					if (input.equals("I'm Out")) {
						disconnect();
					} else {
						handleInput(input);
					}
				} else {
					System.out.println("null received");
					disconnect();
				}
			} catch(Exception e) {
				System.out.println("problem occured");
				e.printStackTrace();
				disconnect();
			}
		}
	}
	
	private void handleInput(String s) throws IOException {
		String info[] = s.split("[|]");
		switch(info[0]) {
			case "Add": {
				int doubtId = doubtHandler.getNewId();
				Doubt doubt = new Doubt();
				doubt.lines = Integer.parseInt(info[1]);
				doubt.linesReceived++;
				doubt.name = info[2];
				doubt.rollNo = info[3];
				doubt.setDoubtLine(1, info[4]);
				String time = new SimpleDateFormat("h:mm aa").format(Calendar.getInstance().getTime());
				doubt.time = time;
				doubt.DoubtId = doubtId;
				doubtHandler.addDoubt(doubt);
				s = s + "|" + time + "|" + Integer.toString(doubtId);
				broadcaster.broadcastMessage(s);
				for (int i = 2; i <= doubt.lines; i++) {
					s = in.readLine();
					String inf[] = s.split("[|]");
					if (inf[0].equals("App")) {
						doubtHandler.appendDoubt(
								doubtId,
								Integer.parseInt(info[1]),
								info[2]);
						s = s + "|" + Integer.toString(doubtId);
						broadcaster.broadcastMessage(s);
					}
				}
				break;
			}
			case "Up": {
				s = s + "|" + Integer.toString(doubtHandler.upVoteDoubt(Integer.parseInt(info[2]), info[1]));
				broadcaster.broadcastMessage(s);
				break;
			}
			case "Nup": {
				s = s + "|" + Integer.toString(doubtHandler.nupVoteDoubt(Integer.parseInt(info[2]), info[1]));
				broadcaster.broadcastMessage(s);
				break;
			}
			case "Del": {
				doubtHandler.deleteDoubt(Integer.parseInt(info[1]));
				broadcaster.broadcastMessage(s);
				break;
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
