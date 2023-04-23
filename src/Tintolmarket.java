import java.io.File;
import java.util.NoSuchElementException;
import java.util.Scanner;


/**
 *
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class Tintolmarket {
	
	private static final String TRUSTSTORE_PASSWORD = "password";
	
    public static void main(String[] args) {
    	Scanner inputReader = new Scanner(System.in);
        if (args.length < 5) {
            System.err.println("Not enough arguments.");
            System.exit(-1);
        }
    	
        
        int port = 12345;
        String ipAddress = args[0];
        if (args[0].contains(":")) {
            String[] tokensAddress = args[0].split(":");
            port = Integer.parseInt(tokensAddress[1]);
            ipAddress = tokensAddress[0];
        }
        
        String trustStoreFileName = args[1];
        String keyStoreFileName = args[2];
        String keyStorePassword = args[3];
        String userId = args[4];
    	
    	System.setProperty("javax.net.ssl.trustStore", "userFiles/" + trustStoreFileName);
        System.setProperty("javax.net.ssl.trustStorePassword", TRUSTSTORE_PASSWORD);
        
        TintolStub stub = new TintolStub(ipAddress,port,keyStoreFileName,keyStorePassword);
        boolean res = stub.login(userId);

        if (!res) {
            System.err.println("Wrong password.");
            System.exit(-1);
        }
        
        initialiseFolderStructure();
        
        showMenu();
        try {
        	while (true) {
                String command = inputReader.nextLine();
                String[] tokens = command.split(" ");
                switch (tokens[0]) {
                    case "a":
                    case "add":
                        stub.addWine(tokens);
                        break;
                    case "s":
                    case "sell":
                        stub.sellWine(tokens);
                        break;
                    case "v":
                    case "view":
                        stub.view(tokens, userId);
                        break;
                    case "b":
                    case "buy":
                        stub.buyWine(tokens);
                        break;
                    case "w":
                    case "wallet":
                        stub.wallet();
                        break;
                    case "c":
                    case "classify":
                        stub.classify(tokens);
                        break;
                    case "t":
                    case "talk":
                        stub.talk(tokens);
                        break;
                    case "r":
                    case "read":
                        stub.read();
                        break;
                    case "l":
                    case "list":
                    	stub.list();
                    	break;
                    default:
                    	System.out.println("Invalid command");
                }
            }
        }
        catch (NoSuchElementException e) {
        	System.out.println("Client will now shut down");
        	System.exit(0);
        }
    }
    
    private static void initialiseFolderStructure() {
    	File receivedImagesDir = new File("userFiles");
        if (!receivedImagesDir.exists()) {
    		if (!receivedImagesDir.mkdir()) {
				System.err.println("The userFiles directory could not be created\n");
				System.exit(-1);
    		}
        }
    }

    private static void showMenu() {
        System.out.println("-----Menu-----");
        System.out.println("add <wine> <image>");
        System.out.println("sell <wine> <value> <quantity>");
        System.out.println("view <wine>");
        System.out.println("buy <wine> <seller> <quantity>");
        System.out.println("wallet");
        System.out.println("classify <wine> <stars>");
        System.out.println("talk <user> <message>");
        System.out.println("read");
        System.out.println();
    }

}
