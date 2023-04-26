package Catalogos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Models.Message;

/**
 *
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class CatalogoDeMensagens extends Catalogo {
	private static CatalogoDeMensagens INSTANCE = null;
	private static final String MSGS_FILE = "serverFiles/messages.txt";
	private File messagesFile;
	private Map<String, List<Message>> messages;

	private CatalogoDeMensagens() {
		messagesFile = new File(MSGS_FILE);
		try {
			messagesFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		messages = new HashMap<>();
	}

	public static CatalogoDeMensagens getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CatalogoDeMensagens();
		}
		return INSTANCE;
	}

	public void registerMessage(String toUser, String fromUser, String message) {
		hmac.confirmHmac();
		
		Message msg = new Message(fromUser,message);

		if (messages.containsKey(toUser)) {
			List<Message> userMessages = this.messages.get(toUser);
			StringBuilder targetLine = new StringBuilder(toUser + "-");
			for (Message currentMessage : userMessages) {
				targetLine.append(currentMessage.getSender() + ":" + currentMessage.getText() + ";");
			}
			this.messages.get(toUser).add(msg);
			String toReplace = targetLine + msg.getSender() + ":" + msg.getText();
			changeLine(targetLine.deleteCharAt(targetLine.length()-1).toString(), toReplace, this.messagesFile);
		}
		else {
			List<Message> msgs = new ArrayList<>();
			msgs.add(msg);
			this.messages.put(toUser, msgs);

			try (BufferedWriter writer = new BufferedWriter(new FileWriter(this.messagesFile,true))) {
				writer.append(toUser + "-" + fromUser + ":" + message + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		hmac.writeHmac();
	}

	public String readMessages(String user) {
		hmac.confirmHmac();
		
		if (!messages.containsKey(user)) {
			return "0 new messages.";
		}
		StringBuilder sb = new StringBuilder("New Messages:\n");
		StringBuilder targetLine = new StringBuilder(user + "-");
		List<Message> messagesToRead = messages.get(user);
		for (Message m: messagesToRead) {
			sb.append("Models.Message from: " + m.getSender() + "\nContent: " + m.getText() + "\n\n");
			targetLine.append(m.getSender() + ":" + m.getText() + ";");
		}
		changeLine(targetLine.deleteCharAt(targetLine.length()-1).toString(), null, this.messagesFile);
		
		hmac.writeHmac();
		
		messages.remove(user);
		return sb.toString();
	}

	@Override
	public void load() {
		try (BufferedReader reader = new BufferedReader(new FileReader(messagesFile))) {
			String line = reader.readLine();
			String[] tokens = null;

			while (line != null) {
				tokens = line.split("-");
				String[] userMessages = tokens[1].split(";");
				List<Message> mensagens = new ArrayList<>();
				for (int i = 0; i < userMessages.length; i++) {
					mensagens.add(new Message(userMessages[i]));
				}
				this.messages.put(tokens[0], mensagens);
				line = reader.readLine();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}