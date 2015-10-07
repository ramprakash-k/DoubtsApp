package doubtServer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginHandler {

	Map<String,String> users;
	
	LoginHandler() {
		users = new HashMap<String,String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("./../../Login/loginid.csv"));
			String line = br.readLine();
			while (line != null) {
				String info[] = line.split(",");
				users.put(info[0], info[1]);
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {}
	}
	
	public boolean checkValid(String user,String pass) {
		if (!users.containsKey(user)) return false;
		if (users.get(user).equals(pass)) return true;
		return false;
	}
}
