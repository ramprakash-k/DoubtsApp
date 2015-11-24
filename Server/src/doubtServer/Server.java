package doubtServer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.ScrollPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class Server {
	
	public static JPanel pane;
	public static GridBagLayout layout;
	public static JPanel panel;
	
	public static void initPane(Container container, final DoubtHandler doubtHandler) {
		ScrollPane scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
		panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.LEFT,0,0));
		
		JLabel label0 = new JLabel("Doubt Id", SwingConstants.CENTER);
		label0.setBorder(BorderFactory.createRaisedBevelBorder());
		label0.setPreferredSize(new Dimension(90,30));
		panel.add(label0);
		
		JLabel label1 = new JLabel("Name", SwingConstants.CENTER);
		label1.setBorder(BorderFactory.createRaisedBevelBorder());
		label1.setPreferredSize(new Dimension(140,30));
		panel.add(label1);
		
		JLabel label2 = new JLabel("Roll no", SwingConstants.CENTER);
		label2.setBorder(BorderFactory.createRaisedBevelBorder());
		label2.setPreferredSize(new Dimension(140,30));
		panel.add(label2);
		
		JLabel label3 = new JLabel("Time \u25bd", SwingConstants.CENTER);
		label3.setBorder(BorderFactory.createRaisedBevelBorder());
		label3.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				doubtHandler.sortClickAction(true);
			}
		});
		label3.setPreferredSize(new Dimension(130,30));
		panel.add(label3);
		
		JLabel label4 = new JLabel("Doubt", SwingConstants.CENTER);
		label4.setBorder(BorderFactory.createRaisedBevelBorder());
		label4.setPreferredSize(new Dimension(400,30));
		panel.add(label4);
		
		JLabel label5 = new JLabel("Upvotes", SwingConstants.CENTER);
		label5.setBorder(BorderFactory.createRaisedBevelBorder());
		label5.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				doubtHandler.sortClickAction(false);
			}
		});
		label5.setPreferredSize(new Dimension(90,30));
		panel.add(label5);
		
		pane = new JPanel();
		layout = new GridBagLayout();
		pane.setLayout(layout);
		scrollPane.add(pane);
		
		Box box = Box.createVerticalBox();
		box.add(panel);
		box.add(scrollPane);
		scrollPane.setPreferredSize(new Dimension(950,650));
		container.add(box, BorderLayout.NORTH);
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
		final Broadcaster broadcaster = new Broadcaster();
		final DoubtHandler doubtHandler = new DoubtHandler(broadcaster);
		final LoginHandler loginHandler = new LoginHandler();
		initPane(frame.getContentPane(), doubtHandler);
		frame.pack();
		frame.setVisible(true);
		System.out.println("Server started at port number "+Integer.toString(portNumber));
		while (frame.isVisible()) {
			try {
				Socket clientSocket = serverSocket.accept();
				System.out.println(clientSocket.getInetAddress().getHostAddress() + " connected.");
				new Thread(new ClientHandler(clientSocket,broadcaster,doubtHandler,loginHandler)).start();
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
		doubtHandler.SaveAll();
		System.exit(0);
	}
}
