import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.security.SignedObject;
import logs.SellTransaction;
import logs.BuyTransaction;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import Catalogos.CatalogoDeVinhos;


/**
 *
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class TintolStub {
    private static final String INVALID_FORMAT = "Invalid Format";
    
    private SSLSocket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private KeyStore keyStore;
    private KeyStore trustStore;
    private PrivateKey privateKey;
    private String userId;
    
    public TintolStub(String ip, int port, String keyStoreFileName, String keyStorePassWord, String trustStoreFileName) {
        this.socket = connectToServer(ip,port);
        this.out = Utils.gOutputStream(socket);
        this.in = Utils.gInputStream(socket);
        try {
			FileInputStream kfile = new FileInputStream("userFiles/" + keyStoreFileName);
			FileInputStream tFile = new FileInputStream("userFiles/" + trustStoreFileName);
			this.keyStore = KeyStore.getInstance("JKS");
			this.keyStore.load(kfile, "password".toCharArray());
			this.trustStore = KeyStore.getInstance("JKS");
			this.trustStore.load(tFile, "password".toCharArray());
	        String alias = this.keyStore.aliases().nextElement();
	        this.privateKey = (PrivateKey) this.keyStore.getKey(alias, keyStorePassWord.toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableKeyException e) {
			e.printStackTrace();
		}
    }
    
    private static SSLSocket connectToServer(String ip, int port) {
        SocketFactory sf = SSLSocketFactory.getDefault();
        SSLSocket s = null;
		try {
			s = (SSLSocket) sf.createSocket(ip, port);
		} catch (IOException e) {
			e.printStackTrace();
		}    
        return s;
    }

    public boolean login(String userId) {
    	boolean res = false;
        boolean iExist = false;
        this.userId = userId;
        String loginMessage = null;
        Long nonce = 0L;
        try {
            this.out.writeObject(userId);
            nonce = (Long) this.in.readObject();
            iExist = (boolean) this.in.readObject();
            
        	Signature s = Signature.getInstance("MD5withRSA");
        	s.initSign(this.privateKey);
        	s.update(nonce.byteValue());
        	byte[] signedNonce = s.sign();
            if (iExist) {
            	out.writeObject(signedNonce);
            }
            else {
            	out.writeObject(nonce);
            	out.writeObject(signedNonce);
            	String alias = this.keyStore.aliases().nextElement();
            	Certificate myCertificate = this.keyStore.getCertificate(alias);
            	out.writeObject(myCertificate);
            }
            loginMessage = (String) in.readObject();
            res = (boolean) in.readObject();
            System.out.println(loginMessage);
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | KeyStoreException e) {
            e.printStackTrace();
        }
        return res;
    }

    public void addWine(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println(INVALID_FORMAT);
            return;
        }

        if(!ValidationLib.verifyString(tokens[1])) {
        	System.out.println("The name of the wine is not valid.");
            return;
        }

        if (!ValidationLib.hasValidExtension(tokens[2])) {
            System.out.println("The image needs to have a valid extension: jpg, jpeg, png.");
            return;
        }

        File image = new File(tokens[2]);
        System.out.println(image);
        if (!image.exists()) {
        	System.out.println("Typed image does not exist.");
            return;
        }

        String extension = Utils.getFileExtension(tokens[2]);

        String wine[] = {tokens[0],tokens[1], extension};
        boolean res = false;
		String serverAnswer = null;
        try {
			this.out.writeObject(String.join(" ", wine));
			res = (boolean) this.in.readObject();
			if (!res) {
                serverAnswer = (String) this.in.readObject();
                System.out.println(serverAnswer);
                return;
            }
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		//Envia o seu ficheiro
		File f = new File(tokens[2]);
		FileInputStream fin = null;
		try {
            fin = new FileInputStream(f);
			InputStream input = new BufferedInputStream(fin);
			byte[] buffer = new byte[(int) f.length()];
			input.read(buffer);

			out.writeObject(buffer);

			fin.close();
			input.close();

			serverAnswer = (String) this.in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

        //If everything went ok then the server will answer with a success message, otherwise, the error message.
        System.out.println(serverAnswer);
        System.out.println();
    }

    public void sellWine(String[] tokens) {
        if (tokens.length != 4) {
            System.out.println(INVALID_FORMAT);
            return;
        }
        
        if(!ValidationLib.verifyString(tokens[1])) {
        	System.out.println("The name of the wine is not valid.");
            return;
        }

        if (!ValidationLib.isValidNumber(tokens[2])) {
            System.out.println("The value of the wine needs to be a number.");
            return;
        }

        if (!ValidationLib.isIntegerNumber(tokens[3])) {
            System.out.println("The quantity of the wine needs to be an integer.");
            return;
        }
        
        String res = null;
		try {
			Signature signature = Signature.getInstance("MD5withRSA");
			SellTransaction sellTransaction = new SellTransaction(tokens[1], tokens[3], tokens[2], this.userId);
	        SignedObject transactionToSend = new SignedObject(sellTransaction, this.privateKey, signature);
	        this.out.writeObject("sell");
	        this.out.writeObject(transactionToSend);
            res = (String) this.in.readObject();
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
        
//        String res = sendReceive(tokens);
        //If everything went ok then the server will answer with a success message, otherwise, the error message.
        System.out.println(res);
        System.out.println();
    }

    public void view(String[] tokens, String user) {
        if (tokens.length != 2) {
            System.out.println(INVALID_FORMAT);
            return;
        }

        if(!ValidationLib.verifyString(tokens[1])) {
        	System.out.println("The name of the wine is not valid.");
            return;
        }

        String res = "";
        try {
            this.out.writeObject(String.join(" ", tokens));
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean existsWine = false;
        String extension = null;
        String imageDir = null;
        try {
        	existsWine = (boolean) in.readObject();
		} catch (ClassNotFoundException | IOException e1) {
			e1.printStackTrace();
		}

        if (existsWine) {
        	try {
        		extension = (String) in.readObject();
        		imageDir = "userFiles/" + tokens[1] + "_" + user + "." + extension;
        		byte[] fileContent = (byte[]) this.in.readObject();
        		FileOutputStream fos = new FileOutputStream(new File(imageDir));
        		fos.write(fileContent);
        		fos.close();
        	} catch (IOException | ClassNotFoundException e) {
        		e.printStackTrace();
        	}
        }

		try {
			res = (String) in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}

        //If everything went ok then the server will answer with a success message, otherwise, the error message.
        System.out.println(res);
        if(existsWine) System.out.println("The image was saved on: " + imageDir);
        System.out.println();
    }

    public void buyWine(String[] tokens) {
        if (tokens.length != 4) {
            System.out.println(INVALID_FORMAT);
            return;
        }

        if(!ValidationLib.verifyString(tokens[1])) {
        	System.out.println("The name of the wine is not valid.");
            return;
        }

        if(!ValidationLib.verifyString(tokens[2])) {
        	System.out.println("The name of the seller is not valid.");
            return;
        }

        if(!ValidationLib.isIntegerNumber(tokens[3])) {
            System.out.println("The quantity of the wine needs to be an integer.");
            return;
        }
        
        String res = null;
		try {
			Signature signature = Signature.getInstance("MD5withRSA");
			
	        this.out.writeObject("buy");
	        //buy vinho seller qtd
	        this.out.writeObject(tokens[1]); //send wine name
	        this.out.writeObject(tokens[2]); //send seller name
	        this.out.writeObject(tokens[3]); //send quantity
	        this.out.writeObject(this.userId); //send user name
	        String winePrice = (String) this.in.readObject();
	        
	        if (!winePrice.equals("-1")) {
	        	BuyTransaction buyTransaction = new BuyTransaction(tokens[1],  tokens[3], winePrice,this.userId);
		        SignedObject transactionToSend = new SignedObject(buyTransaction, this.privateKey, signature);
		        this.out.writeObject(transactionToSend);
            }
            res = (String) this.in.readObject();
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

        //String res = sendReceive(tokens);
        //If everything went ok then the server will answer with a success message, otherwise, the error message.
        System.out.println(res);
        System.out.println();
    }

    public void wallet() {
        String balance = null;
        try {
            this.out.writeObject("wallet");
            balance = (String) this.in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println(balance);
        System.out.println();
    }

    public void classify(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println(INVALID_FORMAT);
            return;
        }

        if(!ValidationLib.verifyString(tokens[1])) {
        	System.out.println("The name of the wine is not valid.");
            return;
        }

        if(!ValidationLib.isIntegerNumber(tokens[2]) || Integer.parseInt(tokens[2]) < 1 || Integer.parseInt(tokens[2]) > 5) {
            System.out.println("The rating has to be an integer between 1 and 5.");
            return;
        }

        String res = sendReceive(tokens);
        //If everything went ok then the server will answer with a success message, otherwise, the error message.
        System.out.println(res);
        System.out.println();
    }

    public void talk(String[] tokens) {
        if (tokens.length < 3) {
            System.out.println(INVALID_FORMAT);
            return;
        }
        
        if(!ValidationLib.verifyString(tokens[1])) {
        	System.out.println("The name of the user is not valid.");
            return;
        }
        
        //Primeiro, obter a mensagem que se pretende enviar
        StringBuilder sb = new StringBuilder();
        for(int i = 2; i < tokens.length; i++) { //message starts in tokens[2]
        	sb.append(tokens[i]+" ");
        }
        String message = sb.deleteCharAt(sb.length()-1).toString();
        
        
        byte[] encrypted = null;
        
        //Encriptar a mensagem, para tal, vou usar a PublicKey do User para que eu quero enviar
        
        try {
        	//1 - Obter a public key a partir da truststore
			PublicKey userToSendPK = this.trustStore.getCertificate(tokens[1]).getPublicKey();
			Cipher c = Cipher.getInstance("RSA");
			c.init(Cipher.ENCRYPT_MODE, userToSendPK);
			//Codificar a mensagem a enviar
			encrypted = c.doFinal(message.getBytes());
			//Send the message to the server
			this.out.writeObject("talk" + " " + tokens[1]);
			this.out.writeObject(Base64.getEncoder().encodeToString(encrypted));
		} catch (KeyStoreException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | IOException e) {
			e.printStackTrace();
		}
        
        //Send the encrypted message to the server
        String res = null;
        try {
			res = (String) this.in.readObject();
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
        //If everything went ok then the server will answer with a success message, otherwise, the error message.
        System.out.println(res);
        System.out.println();
    }

    public void read() {
        String res[] = null;
        try {
            this.out.writeObject("read");
            res = (String[]) this.in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (res == null) {
        	System.out.println("No new messages");
        	System.out.println();
        	return;
        }
        //Decrypt it
        StringBuilder sb = new StringBuilder();
        Cipher d = null;
		try {
			d = Cipher.getInstance("RSA");
			d.init(Cipher.DECRYPT_MODE, this.privateKey);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}
        for (String message : res) {
        	String[] tokens = message.split(":");
        	byte[] decrypted = null;
        	byte[] decoded = Base64.getDecoder().decode(tokens[1]);
    		try {
    			decrypted = d.doFinal(decoded);
    			sb.append("From: " + tokens[0] + " -> " + new String(decrypted) + "\n");
    		} catch (IllegalBlockSizeException | BadPaddingException e) {
    			e.printStackTrace();
    		}
        }
        System.out.println(sb.toString());
        //If everything went ok then the server will answer with the user's messages, otherwise
        //it will say that there are no messages.
        System.out.println();
    }
    
    public void list() {
    	String res = "";
    	try {
            this.out.writeObject("list");
            res = (String) this.in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    	System.out.println(res);
    }

    private String sendReceive(String[] tokens) {
        String res = "";
        try {
            this.out.writeObject(String.join(" ", tokens));
            res = (String) this.in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }

}









