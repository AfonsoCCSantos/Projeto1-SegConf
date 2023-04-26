package Catalogos;

import Models.WineSell;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class CatalogoVendas extends Catalogo {

	private static CatalogoVendas INSTANCE = null;
	private static final String SELLS_FILE = "serverFiles/sells.txt";
	private File sells;
	private Map<String,List<WineSell>> sellsMap;

	private CatalogoVendas() {
		sells = new File(SELLS_FILE);
		try {
			sells.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		sellsMap = new HashMap<>();
	}


	public static CatalogoVendas getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CatalogoVendas();
		}
		return INSTANCE;
	}

	/*
	 * @requires wineExists(wine)
	 */
	public boolean userSellsWine(String user, String wineName) {
		List<WineSell> wineSells = sellsMap.get(wineName);
		for (WineSell sell : wineSells) {
			if (sell.getSeller().equals(user)) {
				return true;
			}
		}
		return false;
	}

	public void sellWine(String wine, String user, String value, String quantity) {
		hmac.confirmHmac();
		
		String newRow = null;

		try(BufferedWriter writer = new BufferedWriter(new FileWriter(sells,true))) {
			newRow = user + SEPARATOR + wine + SEPARATOR + Double.parseDouble(value) + SEPARATOR + quantity;
    		writer.append(newRow+"\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		hmac.writeHmac();

		if (!sellsMap.containsKey(wine)) {
			sellsMap.put(wine, new ArrayList<WineSell>());
		}
		sellsMap.get(wine).add(new WineSell(newRow));
	}

	/**
	 *
	 * @param wine
	 * @requires wineExists(wine)
	 * @return
	 */
	public double getWinePrice(String wine, String seller) {
		double price = 0;
		List<WineSell> wineSells = sellsMap.get(wine);
		for (WineSell w : wineSells) {
			if(w.getSeller().equals(seller)) {
				price = w.getPrice();
				break;
			}
		}
		return price;
	}

	public String getWineSalesInfo(String wine) {
		StringBuilder sb = new StringBuilder();
		if (sellsMap.containsKey(wine)) {
			List<WineSell> wineSells = sellsMap.get(wine);

			for (WineSell w : wineSells) {
				sb.append(w.getSellInfo());
			}
		}

		if (sb.length() == 0) {
			return "No sales of this wine";
		}
		return "Units being sold:\n" + sb.toString() +  "\n";
	}


	public void removeQuantityFromSell(String wine, String seller, String quantity) {
		hmac.confirmHmac();
		
		List<WineSell> sellsList = this.sellsMap.get(wine);
		WineSell targetSell = null;
		for (WineSell s : sellsList){
			if(s.getSeller().equals(seller)){
				targetSell = s;
				break;
			}
		}
		if (targetSell.getQuantity() == Integer.parseInt(quantity)) {
			sellsList.remove(targetSell);
			String targetLine = targetSell.getLine();
			changeLine(targetLine, null, sells);
			//remove sellRow
		}
		else { // remove uma quantidade
			String targetLine = targetSell.getLine();
			targetSell.removeQuantity(Integer.parseInt(quantity));
			String toReplace = targetSell.getLine();
			changeLine(targetLine, toReplace, sells);
		}
		
		hmac.writeHmac();
	}


	@Override
	public void load() {
		
		try (BufferedReader reader = new BufferedReader(new FileReader(sells))) {
			String line = reader.readLine();
			String[] tokens = null;
			while (line != null) {
				tokens = line.split(SEPARATOR);
				if (!sellsMap.containsKey(tokens[0])) {
					sellsMap.put(tokens[1], new ArrayList<WineSell>());
				}
				sellsMap.get(tokens[1]).add(new WineSell(line));
				line = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
