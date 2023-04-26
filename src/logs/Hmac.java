package logs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;

import javax.crypto.Mac;

public class Hmac {
	
	private static Hmac INSTANCE = new Hmac();
	private String WINES = "serverFiles/wines.txt";
	private String SELLS = "serverFiles/sells.txt";
	private String MESSAGES = "serverFiles/messages.txt";
	private String BUDGETS = "serverFiles/budgets.txt";
	private File macs;
	private Key key;
	
	private Hmac() {
		macs = new File("serverFiles/macs.txt");
		if (!macs.exists()) {
			try {
				macs.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public static Hmac getInstance() {
		return INSTANCE;
	}
	
	public void setKey(Key k) {
		this.key = k;
	}
	
	private byte[] getContentAllFiles() {
		byte[] contentSells = null;
		byte[] contentWines = null;
		byte[] contentBudgets = null;
		byte[] contentMessages = null;
		try {
			contentWines = Files.readAllBytes(Paths.get(WINES));
			contentSells = Files.readAllBytes(Paths.get(SELLS));
			contentBudgets = Files.readAllBytes(Paths.get(BUDGETS));
			contentMessages = Files.readAllBytes(Paths.get(MESSAGES));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] content = null;
        try {
        	outputStream.write(contentWines);
        	outputStream.write(contentSells);
        	outputStream.write(contentBudgets);
			outputStream.write(contentMessages);
			content = outputStream.toByteArray();
			outputStream.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        return content;
	}
	
	
	public void writeHmac() {
	
		byte[] content = getContentAllFiles();
 
		
		try {
			FileOutputStream fos = new FileOutputStream(macs);
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(key);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			
			mac.update(content);
			oos.writeObject(mac.doFinal( ));
			fos.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void confirmHmac() {
		if (!verifyMac()) {
			System.out.println("HMAC ERROR!");
			System.exit(-1);
		}
	}
	
	public boolean verifyMac() {
		System.out.println("VERIFYING HMAC");
		
		if (macs.length() == 0) {
			writeHmac();
		}
		
		boolean verified = false;
		
		byte[] content = getContentAllFiles();
		
		try {
			FileInputStream fos = new FileInputStream(macs);
			ObjectInputStream oos = new ObjectInputStream(fos);
			
			byte[] hmac = (byte[]) oos.readObject();
			
			
			Mac mac = Mac.getInstance("HmacSHA256");
			
			mac.init(key);
			mac.update(content);
			byte[] hmacNew = mac.doFinal( );
			
			oos.close();
			fos.close();
			
			
			
			verified = new String(hmac).equals(new String(hmacNew));
			if (verified) System.out.println("CORRECT HMAC");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return verified;
	}

}
