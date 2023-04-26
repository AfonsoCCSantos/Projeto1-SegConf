package Catalogos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CatalogoDeSaldos extends Catalogo {
	private static final double STARTING_BUDGET = 200;
	private static CatalogoDeSaldos INSTANCE = null;
	private static final String BUDGETS_FILE = "serverFiles/budgets.txt";
	private File budgets;
	private Map<String, Double> usersBudget;

	private CatalogoDeSaldos() {
		budgets = new File(BUDGETS_FILE);
		usersBudget = new HashMap<>();

		try {
			budgets.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean userExists(String user) {
		return usersBudget.containsKey(user);
	}

	public void registerUser(String user) {
		hmac.confirmHmac();
		
		String budgetRow = null;

		try {
			BufferedWriter writer = null;
			writer = new BufferedWriter(new FileWriter(budgets, true));
			budgetRow = user + SEPARATOR + STARTING_BUDGET;
			writer.append(budgetRow+"\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		hmac.writeHmac();
		
		usersBudget.put(user, STARTING_BUDGET);

	}

	public static CatalogoDeSaldos getInstance()	{
		if (INSTANCE == null) {
			INSTANCE = new CatalogoDeSaldos();
		}
		return INSTANCE;
	}

	public boolean userHasMoney(String user, double value) {
		return usersBudget.get(user) >= value;
	}

	public void updateMoney(String user, double value) {
		hmac.confirmHmac();
		
		double newValue = usersBudget.get(user) +value;

		String target = user + SEPARATOR + usersBudget.get(user);
		String replacement = user + SEPARATOR + newValue;
		changeLine(target, replacement, budgets);
		
		hmac.writeHmac();

		usersBudget.put(user, newValue);
	}

	//@requires users exist
	public double getUserMoney(String user) {
		return usersBudget.get(user);
	}

	@Override
	public void load() {
		try (BufferedReader reader = new BufferedReader(new FileReader(budgets))) {
			String line = reader.readLine();
			String[] tokens = null;
			while (line != null) {
				tokens = line.split(SEPARATOR);
				usersBudget.put(tokens[0], Double.parseDouble(tokens[1]));
				line = reader.readLine();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

}