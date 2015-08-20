package doubtServer;

import java.util.Observable;

public class Broadcaster extends Observable {
	public void broadcastMessage(String message) {
		setChanged();
		notifyObservers(message);
	}
}
