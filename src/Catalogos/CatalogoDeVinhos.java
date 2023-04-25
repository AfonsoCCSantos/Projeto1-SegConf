package Catalogos;

import Models.Wine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class CatalogoDeVinhos extends Catalogo {

	private static CatalogoDeVinhos INSTANCE = null;
	private static final String WINE_FILE = "serverFiles/wines.txt";
	private static File wines;
	private Map<String, Wine> winesMap;
	

	private CatalogoDeVinhos() {
		wines = new File(WINE_FILE);
		try {
			wines.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		winesMap = new HashMap<>();
	}

	public static CatalogoDeVinhos getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CatalogoDeVinhos();
		}
		return INSTANCE;
	}

	public Wine getWine(String wine) {
		return winesMap.get(wine);
	}

	public boolean registerWine(String wine, String imagePath) {
		if (wineExists(wine)) return false;
		String wineRow = null;
		this.hmac.confirmHmac();
		try {
			BufferedWriter writer = null;
			writer = new BufferedWriter(new FileWriter(wines, true));
			wineRow = wine + SEPARATOR + imagePath + SEPARATOR + "0" + SEPARATOR + "0" + SEPARATOR + "0";
			writer.append(wineRow+"\n");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.hmac.writeHmac();
		winesMap.put(wine, new Wine(wineRow));
		return true;
	}

	public void classifyWine(String wineName, int stars) {
		hmac.confirmHmac();
		Wine wine = winesMap.get(wineName);
		String targetLine = wine.getLine();
		wine.classify(stars);
		String toReplace = wine.getLine();
		changeLine(targetLine,toReplace, wines);
		hmac.writeHmac();
	}

	public boolean wineExists(String wine) {
		return this.winesMap.containsKey(wine);
	}


	/**
	 *
	 * @param wine
	 * @requires wineExistis(wine)
	 * @return
	 */
	public String getWineInfo(String wine) {
		Wine w = this.winesMap.get(wine);
		return w.getInfo();
	}

	public boolean hasEnoughQuantity(String wine, int quantity) {
		return this.winesMap.get(wine).getQuantity() >= quantity;
	}

	//@requires wineExists(wine)
	public void updateQuantity(String wine, int quantity) {
		String line = null;
		int newQuantity = 0;
		StringBuilder sb = new StringBuilder();

		hmac.confirmHmac();
		try (BufferedReader reader = new BufferedReader(new FileReader(wines))){
			line = reader.readLine();
			String[] tokens = null;
			while (line != null) {
				tokens = line.split(SEPARATOR);
				if (!tokens[0].equals(wine)) {
					sb.append(line + "\n");
				}
				else {
					newQuantity = Integer.parseInt(tokens[4]) + quantity;
					sb.append(tokens[0] + SEPARATOR + tokens[1] +  SEPARATOR + tokens[2] + SEPARATOR +  tokens[3] + SEPARATOR + newQuantity + "\n");
				}
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		//Now, write the lines into the file
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(wines))) {
			writer.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		hmac.writeHmac();

		this.winesMap.get(wine).updateUnits(quantity);
	}

	@Override
	public void load() {
		hmac.confirmHmac();
		try (BufferedReader reader = new BufferedReader(new FileReader(wines))) {
			String line = reader.readLine();
			String[] tokens = null;
			while (line != null) {
				tokens = line.split(SEPARATOR);
				winesMap.put(tokens[0], new Wine(line));
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
