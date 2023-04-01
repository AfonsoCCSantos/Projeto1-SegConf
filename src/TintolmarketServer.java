import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


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
        int port = args.length == 0 ? 12345 : Integer.parseInt(args[0]);
        startServer(port);
    }
    
    public static void startServer(int port) {
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
				ServerThread newServerThread = new ServerThread(inSocket);
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
