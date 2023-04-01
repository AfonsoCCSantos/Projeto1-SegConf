package Models;

/**
 * 
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class Wine {
	
	private static final String SEPARATOR = ":";
	private String name;
	private String image;
	private int totalRating;
	private int numOfRatings;
	private int units;
	
	
	public Wine(String wineRow) {
		String[] tokens = wineRow.split(SEPARATOR);
		this.name = tokens[0];
		this.image = tokens[1];
		this.totalRating = Integer.parseInt(tokens[2]);
		this.numOfRatings = Integer.parseInt(tokens[3]);
		this.units = Integer.parseInt(tokens[4]);
	}
	
	public double getRating() {
		if (this.numOfRatings == 0) {
			return -1;
		} 
		return this.totalRating / this.numOfRatings;
	}
	
	public void updateUnits(int quantity){
		this.units += quantity;
	}
	
	public String getInfo(){
		return "Nome: " + this.name + 
			" | Classficacao: " + (getRating() < 0 ? "No rating" : getRating())  + " | Quantidade: " + units + " unidades\n"; 
	}
	
	public int getQuantity() {
		return this.units;
	}
	
	public String getImage() {
		return this.image;
	}
	
	public int getTotalRating() {
		return this.totalRating;
	}
	
	public int getNumOfRatings() {
		return this.numOfRatings;
	}
	
	public void classify(int stars){
		this.numOfRatings +=1 ;
		this.totalRating += stars;
	}
	
	public String getLine() {
		return this.name + SEPARATOR + this.image + SEPARATOR + this.totalRating + SEPARATOR +
				this.numOfRatings + SEPARATOR + this.units;
	}
	
}
