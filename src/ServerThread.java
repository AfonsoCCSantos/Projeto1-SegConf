import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.Socket;


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


    public ServerThread(Socket inSocket, SecretKey passwordKey) {
        socket = inSocket;
		this.passwordKey = passwordKey;
    }

    public void run() {
        TintolSkel skel = new TintolSkel(socket, passwordKey);

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
        		case "s":
        			if (tokens.length != 4 || !ValidationLib.verifyString(tokens[1]) ||
        				!ValidationLib.isValidNumber(tokens[2]) || !ValidationLib.isIntegerNumber(tokens[3])) {
        				closeConnection();
            			return;
        			}
        			skel.sellWine(user, tokens[1], tokens[2], tokens[3]);
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
        		case "b":
        			if (tokens.length != 4 || !ValidationLib.verifyString(tokens[1])
        								   || !ValidationLib.isIntegerNumber(tokens[3])) {
        				closeConnection();
            			return;
        			}
        			skel.buy(user,tokens[1],tokens[2],tokens[3]);
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
        			if (tokens.length < 3) {
        				closeConnection();
            			return;
        			}
        			StringBuilder sb = new StringBuilder();
        	        for(int i = 2; i < tokens.length; i++) { //message starts in tokens[2]
        	        	sb.append(tokens[i]+" ");
        	        }
        	        String message = sb.deleteCharAt(sb.length()-1).toString();
             		skel.talk(user, tokens[1],message);
        			break;
        		case "read":
        		case "r":
             		skel.read(user);
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
