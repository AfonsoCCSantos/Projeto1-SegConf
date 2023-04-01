import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


/**
 * 
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class CatalogoDeUtilizadores extends Catalogo {
	private static CatalogoDeUtilizadores INSTANCE = null;
	private static final String USERS_FILE = "serverFiles/users.txt";
	private File users;
	private Set<String> registeredUsers;

	private CatalogoDeUtilizadores() {
		users = new File(USERS_FILE);
		registeredUsers = new HashSet<>();

		try {
			users.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static CatalogoDeUtilizadores getInstance()	{
		if (INSTANCE == null) {
			INSTANCE = new CatalogoDeUtilizadores();
		}
		return INSTANCE;
	}
	
	public boolean loginUser(String user, String password) {
		BufferedReader reader = null;
		BufferedWriter writer = null;

		try {
			reader = new BufferedReader(new FileReader(users));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		boolean res = false;
		try {
			String line = reader.readLine();
			while (line != null && !line.split(SEPARATOR)[0].equals(user)) {
				line = reader.readLine();
			}
			reader.close();

			//User doesn't exist
			if (line == null) {
				writer = new BufferedWriter(new FileWriter(users, true));
				writer.append(user + SEPARATOR + password + "\n");
				writer.close();
				res = true;
				registeredUsers.add(user);
			} 
			else {
				res = line.split(SEPARATOR)[1].equals(password); 
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public boolean userExists(String user) {
		return registeredUsers.contains(user);
	}

	@Override
	public void load() {
		try (BufferedReader reader = new BufferedReader(new FileReader(users))) {
			String line = reader.readLine();
			String[] tokens = null;
			while (line != null) {
				tokens = line.split(SEPARATOR);
				registeredUsers.add(tokens[0]);
				line = reader.readLine();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}