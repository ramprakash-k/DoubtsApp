package doubtServer;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ScrollPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Server {
	
	public static JPanel pane;
	
	public static void initPane(Container container) {
		ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		container.add(scrollPane);
		pane = new JPanel();
		pane.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weighty = 1;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		
		JLabel label0 = new JLabel("Doubt Id");
		label0.setBorder(BorderFactory.createRaisedBevelBorder());
		gbc.weightx = 1;gbc.gridx = 0;gbc.gridy = 0;
		pane.add(label0, gbc);
		
		JLabel label1 = new JLabel("Name");
		label1.setBorder(BorderFactory.createRaisedBevelBorder());
		gbc.weightx = 1;gbc.gridx = 1;gbc.gridy = 0;
		pane.add(label1, gbc);
		
		JLabel label2 = new JLabel("Roll no");
		label2.setBorder(BorderFactory.createRaisedBevelBorder());
		gbc.weightx = 1;gbc.gridx = 2;gbc.gridy = 0;
		pane.add(label2, gbc);
		
		JLabel label3 = new JLabel("Time");
		label3.setBorder(BorderFactory.createRaisedBevelBorder());
		gbc.weightx = 1;gbc.gridx = 3;gbc.gridy = 0;
		pane.add(label3, gbc);
		
		JLabel label4 = new JLabel("Doubt");
		label4.setBorder(BorderFactory.createRaisedBevelBorder());
		gbc.weightx = 5;gbc.gridx = 4;gbc.gridy = 0;
		pane.add(label4, gbc);
		
		JLabel label5 = new JLabel("Upvotes");
		label5.setBorder(BorderFactory.createRaisedBevelBorder());
		gbc.weightx = 1;gbc.gridx = 5;gbc.gridy = 0;
		pane.add(label5, gbc);
		scrollPane.add(pane);
	}
	
	static ServerSocket serverSocket = null;
	
	public static void main(String[] args) {
        
		int portNumber;
		if (args.length < 1) portNumber = 8000;
		else portNumber = Integer.parseInt(args[0]);
		try {
			serverSocket = new ServerSocket(portNumber);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		JFrame frame = new JFrame("DoubtsApp Server");
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent event) {
				try {
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		frame.setMinimumSize(new Dimension(1000, 700));
		frame.setMaximumSize(new Dimension(1000, 700));
		frame.setResizable(false);
		initPane(frame.getContentPane());
		frame.pack();
		frame.setVisible(true);
		final Broadcaster broadcaster = new Broadcaster();
		final DoubtHandler doubtHandler = new DoubtHandler(broadcaster);
		System.out.println("Server started at port number "+Integer.toString(portNumber));
		while (frame.isVisible()) {
			try {
				Socket clientSocket = serverSocket.accept();
				System.out.println(clientSocket.getInetAddress().getHostAddress() + " connected.");
				new Thread(new ClientHandler(clientSocket,broadcaster,doubtHandler)).start();
			} catch(IOException e) {
				try {
					serverSocket.close();
				} catch (IOException e1) {
					System.out.println("poop1");
					e1.printStackTrace();
					break;
				}
				System.out.println("Socket Closed");
				break;
			}
		}
		System.exit(0);
	}
}
