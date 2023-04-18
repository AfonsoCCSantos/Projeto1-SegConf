import Catalogos.*;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


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

    public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("Not enough arguments given.");
			System.exit(-1);
		}
		int port = 12345;
		String passwordCipher;
		String keystore;
		String keyStorePassword;
		if(args.length == 4) {
			port = Integer.parseInt(args[0]);
			passwordCipher = args[1];
			keystore = args[2];
			keyStorePassword = args[3];
		}
		else {
			passwordCipher = args[0];
			keystore = args[1];
			keyStorePassword = args[2];
		}

		SecretKey passwordKey = getPasswordKey(passwordCipher);
        startServer(port, passwordKey);
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

    public static void startServer(int port, SecretKey passwordKey) {
    	File serverDir = new File(SERVER_DIR);
    	if (!serverDir.exists()) {
    		if (!serverDir.mkdir()) {
				System.err.println("The server directory could not be created\n");
				System.exit(-1);
    		}
    		File serverDirImages = new File(SERVER_DIR_IMAGES);
    		if (!serverDirImages.mkdir()) {
    			System.err.println("The images directory could not be created\n");
				System.exit(-1);
    		}
    	}

        ServerSocket serverSocket = initSocket(port);
        CatalogoDeMensagens.getInstance().load();
        CatalogoDeVinhos.getInstance().load();
        CatalogoVendas.getInstance().load();
        CatalogoDeUtilizadores.getInstance().load();
        CatalogoDeSaldos.getInstance().load();

        while (true) {
            try {
				Socket inSocket = serverSocket.accept();
				ServerThread newServerThread = new ServerThread(inSocket, passwordKey);
				newServerThread.start();
		    }
		    catch (IOException e) {
		        e.printStackTrace();
		    }
        }

    }

    private static ServerSocket initSocket(int port) {
        ServerSocket serverSocket = null;
        try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
        return serverSocket;
    }



}
