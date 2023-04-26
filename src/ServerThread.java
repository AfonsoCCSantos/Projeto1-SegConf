import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.security.Key;
import java.security.SignedObject;

import javax.crypto.SecretKey;


/**
 *
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class ServerThread extends Thread {

    private Socket socket = null;
	private SecretKey passwordKey = null;
	private Key privateKey;


    public ServerThread(Socket inSocket, SecretKey passwordKey, Key privateKey) {
        socket = inSocket;
		this.passwordKey = passwordKey;
		this.privateKey = privateKey;
		
    }

    public void run() {
        TintolSkel skel = new TintolSkel(socket, passwordKey,privateKey);

        String user = skel.loginUser();
        if (user == null) {
        	try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        	return;
        }
        
        //The client application will now print the menu
        String command = null;
        	while (true) {
        		command = skel.getLine();
        		if (command == null) {
        			closeConnection();
        			return;
        		}
        		String[]tokens = command.split(" ");
        		switch (tokens[0]) {
        		case "add":
        		case "a":
        			if (tokens.length != 3 || !ValidationLib.verifyString(tokens[1])) {
        				closeConnection();
            			return;
        			}
        			skel.addWine(tokens[1], "serverFiles/images/"+tokens[1]+"_server." + tokens[2]);
        			break;
        		case "sell":
//        			if (tokens.length != 4 || !ValidationLib.verifyString(tokens[1]) ||
//        				!ValidationLib.isValidNumber(tokens[2]) || !ValidationLib.isIntegerNumber(tokens[3])) {
//        				closeConnection();
//            			return;
//        			}
//        			skel.sellWine(user, tokens[1], tokens[2], tokens[3]);
					SignedObject sellTransaction = null;
					sellTransaction = (SignedObject) skel.getTransaction();
        			skel.sellWine(sellTransaction);
        			break;
        		case "view":
        		case "v":
        			if (tokens.length != 2 || !ValidationLib.verifyString(tokens[1])) {
        				closeConnection();
            			return;
        			}
        			skel.view(tokens[1]);
        			break;
        		case "buy":
        			skel.buy();
        			break;
        		case "wallet":
        		case "w":
             		skel.wallet(user);
        			break;
        		case "classify":
        		case "c":
        			if (tokens.length != 3 || !ValidationLib.verifyString(tokens[1])
							   			   || !ValidationLib.isIntegerNumber(tokens[2])) {
        				closeConnection();
            			return;
        			}
             		skel.classify(tokens[1],tokens[2]);
        			break;
        		case "talk":
        		case "t":
        			
//        			if (tokens.length < 3) {
//        				closeConnection();
//            			return;
//        			}
//        			StringBuilder sb = new StringBuilder();
//        	        for(int i = 2; i < tokens.length; i++) { //message starts in tokens[2]
//        	        	sb.append(tokens[i]+" ");
//        	        }
//        	        String message = sb.deleteCharAt(sb.length()-1).toString();
             		skel.talk(user, tokens[1]);
        			break;
        		case "read":
        		case "r":
             		skel.read(user);
        			break;
        		case "list":
        			skel.list();
        			break;	
        		default:
        			closeConnection();
        			return;
        		}
        	}

    }

    private void closeConnection() {
    	try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.interrupt();
    }

}
