package Catalogos;

import javax.crypto.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
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
	private static final String USERS_FILE = "serverFiles/users.cif";
	private File users;
	private Map<String, String> registeredUsers;
	private SecretKey secretKey;

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
		String toEncrypt = user + SEPARATOR + certificateFileName + "\n";
		byte[] data = null;
		byte[] dataParams = null;
		byte[] decryptedData = null;
		byte[] newParams = null;
		byte[] encryptedBytes = null;
		
		//Read the data that was previously on the file
		data = getFileData(users);
		
		if (data.length > 0) { //time to decrypt
			//Get the parameters
			dataParams = getFileData(new File("serverFiles/params.txt"));
			//Now decrypt the data
			decryptedData = decryptData(dataParams, data);
			
			//At this point, the contents of the users files are decrypted
			
			//Lets merge the previous contents of the file with this new line
			byte[] toEncryptBytes = toEncrypt.getBytes();
			byte[] mergedContents = new byte[decryptedData.length + toEncryptBytes.length];
			int offset = 0;
			for (int i = 0; i < decryptedData.length; i++) {
				mergedContents[offset] = decryptedData[i];
				offset++;
			}
			for (int i = 0; i < toEncryptBytes.length; i++) {
				mergedContents[offset] = toEncryptBytes[i];
				offset++;
			}
			
			//At this point, I have all the content of the file in a new byte[], ready to be encrypted and write
			try {
				Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
				c.init(Cipher.ENCRYPT_MODE, secretKey);
				encryptedBytes = c.doFinal(mergedContents);
				newParams = c.getParameters().getEncoded();
			} catch (NoSuchAlgorithmException | IOException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
				e.printStackTrace();
			}
			
			//Now, just write the new encrypted contents to the file
			writeToFile(encryptedBytes, users);
			//And write the new params to a file as well
			writeToFile(newParams, new File("serverFiles/params.txt"));
		}
		else { //There is nothing on the users file, just encrypt this data and write
			try {
				Cipher c = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
				c.init(Cipher.ENCRYPT_MODE, secretKey);
				encryptedBytes = c.doFinal(toEncrypt.getBytes());
				newParams = c.getParameters().getEncoded();
			} catch (NoSuchAlgorithmException | IOException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
				e.printStackTrace();
			}
			
			//Now, just write the new encrypted contents to the file
			writeToFile(encryptedBytes, users);
			
			//And write the new params to a file as well
			writeToFile(newParams, new File("serverFiles/params.txt"));
		}		
		registeredUsers.put(user, certificateFileName);
	}
	
	public boolean userExists(String user) {
		return registeredUsers.containsKey(user);
	}
	
	public String getCertificateFileName(String userId) {
		return registeredUsers.get(userId);
	}

	@Override
	public void load() {
		//First, get all the content of the users file
		byte[] data = null;
		byte[] dataParams = null;
		byte[] decryptedData = null;
		
		data = getFileData(users);
		
		if (data.length > 0) { //decrypt!
			//Get the parameters
			dataParams = getFileData(new File("serverFiles/params.txt"));
			//decrypt the data
			decryptedData = decryptData(dataParams, data);
			
			System.out.println(new String(decryptedData));
			String[] lines = new String(decryptedData).split("\n");
			for (String line : lines) {
				String[] tokens = line.split(SEPARATOR);
				registeredUsers.put(tokens[0],tokens[1]);
			}
		}
	}
	
	private byte[] decryptData(byte[] params, byte[] toDecrypt) {
		byte[] decrypted = null;
		try {
			AlgorithmParameters p = AlgorithmParameters.getInstance("PBEWithHmacSHA256AndAES_128");
			p.init(params);
			Cipher d = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
			d.init(Cipher.DECRYPT_MODE, secretKey, p);
			decrypted = d.doFinal(toDecrypt);
		} catch (NoSuchAlgorithmException | IOException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
			e.printStackTrace();
		}
		return decrypted;
	}
	
	private void writeToFile(byte[] data, File file) {
		try (FileOutputStream fos = new FileOutputStream(file)) {
			fos.write(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] getFileData(File file) {
		byte[] data = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[16];
			int bytesRead;
			while ((bytesRead = fis.read(buffer)) != -1) {
				bos.write(buffer, 0, bytesRead);
			}
			fis.close();
			bos.close();
			data = bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public SecretKey getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(SecretKey secretKey) {
		this.secretKey = secretKey;
	}
}
