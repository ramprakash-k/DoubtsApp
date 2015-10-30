package doubtServer;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

public class ClientHandler implements Observer, Runnable {
	
	Socket client;
	BufferedReader in;
    DataOutputStream out;
    Broadcaster broadcaster;
    DoubtHandler doubtHandler;
    boolean isAlive;
	
	public ClientHandler(Socket clientSocket, Broadcaster broadcaster, DoubtHandler handler) throws IOException {
		client = clientSocket;
		isAlive = true;
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
					if (input.startsWith("GET ")) {
						File file = new File("./../../DoubtsApp.apk");
						int numBytes = (int) file.length();
						FileInputStream inFile = new FileInputStream(file);
						byte[] fileInBytes = new byte[numBytes];
						inFile.read(fileInBytes);
						out.writeBytes("HTTP/1.0 200 Document Follows\r\n");
						System.out.println("HTTP/1.0 200 Document Follows");
						out.writeBytes("Content-Disposition: attachment; filename=DoubtsApp.apk\r\n");
						System.out.println("Content-Disposition: attachment; filename=DoubtsApp.apk");
						out.writeBytes("Content-Type: application/octet-stream\r\n");
						System.out.println("Content-Type: application/octet-stream");
						out.writeBytes("Content-Length: " + numBytes + "\r\n\r\n");
						System.out.println("Content-Length: " + numBytes + "\n");
						out.write(fileInBytes, 0, numBytes);
						inFile.close();
						disconnect();
					} else {
						out.writeBytes("Wrong app. Disconnecting.");
						System.out.println("Wrong app. Disconnecting.");
						disconnect();
					}
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
		while (!client.isClosed() && isAlive) {
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
								Integer.parseInt(inf[1]),
								inf[2]);
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
				doubtHandler.releaseLock(Integer.parseInt(info[1]));
				broadcaster.broadcastMessage(s);
				break;
			}
			case "ELock":
			case "DLock": {
				if (doubtHandler.getLock(Integer.parseInt(info[1]), 2)) {
					System.out.println(s + "|1");
					out.writeBytes(s + "|1\n");
				} else {
					System.out.println(s + "|0");
					out.writeBytes(s + "|0\n");
				}
				break;
			}
			case "RLock": {
				System.out.println(s);
				doubtHandler.releaseLock(Integer.parseInt(info[1]));
				break;
			}
		}
	}
	
	private void disconnect() {
		isAlive = false;
		try {
			client.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		broadcaster.deleteObserver(this);
	}
}
