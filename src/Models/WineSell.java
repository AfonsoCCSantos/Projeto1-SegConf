package Models;

/**
 *
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class WineSell {

	private static final String SEPARATOR = ":";
	private String seller;
	private String wineName;
	private double price;
	private int quantity;

	public WineSell(String sellRow) {
		String[] tokens = sellRow.split(SEPARATOR);
		this.seller = tokens[0];
		this.wineName = tokens[1];
		this.price = Double.parseDouble(tokens[2]);
		this.quantity = Integer.parseInt(tokens[3]);
	}

	public String getSeller(){
		return this.seller;
	}

	public int getQuantity(){
		return this.quantity;
	}

	public void removeQuantity(int quantity){
		this.quantity -= quantity;
	}

	public double getPrice() {
		return this.price;
	}

	public String getSellInfo() {
		return "Seller: " + this.seller + " | Price: " + this.price + " euros | Quantity:" + this.quantity + " units";
	}

	public String getLine() {
		return this.seller + SEPARATOR + this.wineName + SEPARATOR + this.price + SEPARATOR + this.quantity;
	}
}
