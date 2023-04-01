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

    public static void main(String[] args) {
    	Scanner inputReader = new Scanner(System.in);
    	
        if (args.length < 2) {
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
        
        String user = args[1];
        if (!ValidationLib.verifyString(user)) {
        	System.err.println("Invalid user name. It cointains invalid charaters.");
        	System.exit(-1);
        } 
        String password = null;
        if (args.length == 3) {
            password = args[2];
        }
        else {
            System.out.println("Type your password:");
            password = inputReader.nextLine();
        }
        
        TintolStub stub = new TintolStub(ipAddress,port);
        boolean res = stub.login(user, password);
        
        if (!res) {
            System.err.println("Wrong password.");
            System.exit(-1);
        }
        
        File receivedImagesDir = new File("receivedImages");
        if (!receivedImagesDir.exists()) {
    		if (!receivedImagesDir.mkdir()) {
				System.err.println("The received images directory could not be created\n");
				System.exit(-1);
    		}
        }
         
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
                        stub.view(tokens, user);
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
