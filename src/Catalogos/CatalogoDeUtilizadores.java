package Catalogos;

import javax.crypto.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	private Map<String, String> registeredUsers;

	private CatalogoDeUtilizadores() {
		users = new File(USERS_FILE);
		registeredUsers = new HashMap<>();

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
	
	public void registerUser(String user, String certificateFileName) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(users, true))){
			writer.append(user + SEPARATOR + certificateFileName + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		registeredUsers.put(user, certificateFileName);
	}
	
	public boolean loginUser(String user, String password, SecretKey passwordKey) {
		BufferedReader reader = null;
		BufferedWriter writer = null;
		
		try {
			reader = new BufferedReader(new FileReader(users));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		boolean res = false;
		try {
			if (userExists(user)) {
				res = this.registeredUsers.get(user).equals(password);
			}
			else {
				writer = new BufferedWriter(new FileWriter(users, true));
				writer.append(user + SEPARATOR + password + "\n");
				writer.close();
				res = true;
				registeredUsers.put(user, password);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
//		BufferedReader reader = null;
//		BufferedWriter writer = null;
//
//		Cipher encryptCipher = null;
//		Cipher decryptCipher = null;
//		byte[] params = null;
//		try {
//			encryptCipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
//			encryptCipher.init(Cipher.ENCRYPT_MODE, passwordKey);
//			params = encryptCipher.getParameters().getEncoded();
//			AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
//			p.init(params);
//			decryptCipher = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
//			decryptCipher.init(Cipher.DECRYPT_MODE, passwordKey, p);
//		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
//			throw new RuntimeException(e);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		} catch (InvalidAlgorithmParameterException e) {
//			throw new RuntimeException(e);
//		}
//		
//		try {
//			reader = new BufferedReader(new FileReader(users));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//
//		boolean res = false;
//		try {
//			String line = reader.readLine();
//			byte[] encryptedLine;
//			String decryptedLine = null;
//			while (line != null) {
//				System.out.println(line);
//				encryptedLine = line.getBytes();
//				decryptedLine = new String(decryptCipher.doFinal(encryptedLine));
//				if (decryptedLine.split(SEPARATOR)[0].equals(user)) {
//					break;
//				}
//				line = reader.readLine();
//			}
//			reader.close();
//			
//			//User doesn't exist
//			if (decryptedLine == null) {
//				writer = new BufferedWriter(new FileWriter(users, true));
//				byte[] toEncrypt = (user + SEPARATOR + password).getBytes();
//				byte[] encryptedToWrite = encryptCipher.doFinal(toEncrypt);
//				System.out.println(new String(encryptedToWrite) + "\n");
//				writer.append(new String(encryptedToWrite) + "\n");
//				writer.close();
//				res = true;
//				registeredUsers.add(user);
//			}
//			else {
//				res = decryptedLine.split(SEPARATOR)[1].equals(password);
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (IllegalBlockSizeException e) {
//			throw new RuntimeException(e);
//		} catch (BadPaddingException e) {
//			throw new RuntimeException(e);
//		}
//		return res;
	}
	
	public boolean userExists(String user) {
		return registeredUsers.containsKey(user);
	}
	
	public String getCertificateFileName(String userId) {
		return registeredUsers.get(userId);
	}

	@Override
	public void load() {
		//TODO:decrypt
		try (BufferedReader reader = new BufferedReader(new FileReader(users))) {
			String line = reader.readLine();
			String[] tokens = null;
			while (line != null) {
				tokens = line.split(SEPARATOR);
				registeredUsers.put(tokens[0],tokens[1]);
				line = reader.readLine();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
