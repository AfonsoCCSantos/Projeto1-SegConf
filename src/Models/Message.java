package Models;

/**
 *
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class Message {

	private String sender;
	private String text;
	
	public Message(String msgLine) {
		String[] tokens = msgLine.split(":");
		this.sender = tokens[0];
		this.text = tokens[1];
	}

	public Message(String sender, String text) {
		this.sender = sender;
		this.text = text;
	}

	public String getSender() {
		return this.sender;
	}

	public String getText() {
		return this.text;
	}

}
