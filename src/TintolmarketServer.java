import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.spec.InvalidKeySpecException;
import java.io.FileInputStream;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import Catalogos.CatalogoDeMensagens;
import Catalogos.CatalogoDeSaldos;
import Catalogos.CatalogoDeUtilizadores;
import Catalogos.CatalogoDeVinhos;
import Catalogos.CatalogoVendas;
import logs.Blockchain;
import logs.Hmac;


/**
 *
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class TintolmarketServer {
	
	private static final String SERVER_DIR = "serverFiles";
	private static final String SERVER_DIR_IMAGES = SERVER_DIR + "/" + "images";
	private static final String SERVER_DIR_BLOCKCHAIN = SERVER_DIR + "/" + "blockchain";
	private static final String FAILED_INTEGRITY_VERIFICATION_ERROR_MSG = 
			"Failed to verify the integrity of the blockchain";
	
    public static void main(String[] args) {
		if(args.length < 3) {
			System.out.println("Not enough arguments given.");
			System.exit(-1);
		}
		int port = 12345;
		String passwordCipher;
		String keystoreFileName;
		String keyStorePassword;
		if(args.length == 4) {
			port = Integer.parseInt(args[0]);
			passwordCipher = args[1];
			keystoreFileName = args[2];
			keyStorePassword = args[3];
		}
		else {
			passwordCipher = args[0];
			keystoreFileName = args[1];
			keyStorePassword = args[2];
		}

		SecretKey passwordKey = getPasswordKey(passwordCipher);
		System.setProperty("javax.net.ssl.keyStore", SERVER_DIR+"/" + keystoreFileName);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        startServer(port, passwordKey, keystoreFileName, keyStorePassword);
    }

	private static SecretKey getPasswordKey(String password) {
		byte[] salt = { (byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99,
						(byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2 };
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 20);
		SecretKeyFactory kf = null;
		SecretKey key;
		try {
			kf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
			key = kf.generateSecret(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
		return key;
	}
	
    public static void startServer(int port, SecretKey passwordKey, String keyStoreFileName, String keyStorePassword) {
    	File serverDir = new File(SERVER_DIR);
    	if (!serverDir.exists()) {
    		if (!serverDir.mkdir()) {
				System.err.println("The server directory could not be created\n");
				System.exit(-1);
    		}
    	}
    	
		File serverDirImages = new File(SERVER_DIR_IMAGES);
		if (!serverDirImages.exists()) {
			if (!serverDirImages.mkdir()) {
				System.err.println("The images directory could not be created\n");
				System.exit(-1);
			}
		}
		
		File serverDirBlockchain = new File(SERVER_DIR_BLOCKCHAIN);
		if (!serverDirBlockchain.exists()) {
			if (!serverDirBlockchain.mkdir()) {
				System.err.println("The blockchain directory could not be created\n");
				System.exit(-1);
			}
		}
    	//Get privateKey
    	KeyStore keyStore = null;
    	PrivateKey privateKey = null;
    	PublicKey publicKey = null;
    	try {
    		FileInputStream kfile = new FileInputStream("serverFiles/" + keyStoreFileName);
    		keyStore = KeyStore.getInstance("JKS");
    		keyStore.load(kfile, "password".toCharArray());
//			keyStore = KeyStore.getInstance(new File("serverFiles/" + keyStoreFileName), keyStorePassword.toCharArray());
			String alias = keyStore.aliases().nextElement();
	       privateKey = (PrivateKey) keyStore.getKey(alias, keyStorePassword.toCharArray());
	       publicKey = keyStore.getCertificate(alias).getPublicKey();
	       
		} catch (KeyStoreException | NoSuchAlgorithmException | IOException | UnrecoverableKeyException | java.security.cert.CertificateException e) {
			e.printStackTrace();
		}
    	

        SSLServerSocket serverSocket = initSocket(port);
        
        Hmac hmac = Hmac.getInstance();
        hmac.setKey(passwordKey);
        
        CatalogoDeMensagens.getInstance().load();
        CatalogoDeVinhos.getInstance().load();
        CatalogoVendas.getInstance().load();
        CatalogoDeUtilizadores.getInstance().load();
        CatalogoDeSaldos.getInstance().load();
        Blockchain bc = Blockchain.getInstance();
        bc.setPrivateKey(privateKey);
        bc.setPublicKey(publicKey);
        
        hmac.confirmHmac();
        
        if(!bc.verifyIntegrityOfBlockchain()) {
        	System.out.println(FAILED_INTEGRITY_VERIFICATION_ERROR_MSG);
			System.exit(-1);
        }
        
        bc.load();

        while (true) {
            try {
				Socket inSocket = serverSocket.accept();
				ServerThread newServerThread = new ServerThread(inSocket, passwordKey,privateKey);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
        }

    }

    private static SSLServerSocket initSocket(int port) {
		ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
        SSLServerSocket ss = null;
		try {
			ss = (SSLServerSocket) ssf.createServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
       // ServerSocket serverSocket = null;
        //try {
		//	serverSocket = new ServerSocket(port);
		//} catch (IOException e) {
			//System.err.println(e.getMessage());
			//System.exit(-1);
		//}
        return ss;
    }



}
