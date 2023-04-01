import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


/**
 * 
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class TintolStub {
    private static final String INVALID_FORMAT = "Invalid Format";
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    public TintolStub(String ip, int port) {
        this.socket = connectToServer(ip,port);
        System.out.println();
        this.out = Utils.gOutputStream(socket);
        this.in = Utils.gInputStream(socket);
    }
        
    private static Socket connectToServer(String ip, int port) {
        Socket socket = null;
        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return socket;
    }

    public boolean login(String user, String password) {
        boolean res = false;
        try {
            this.out.writeObject(user);
            this.out.writeObject(password);
            res = (boolean) this.in.readObject();
        } catch (IOException | ClassNotFoundException e) {
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
        
        String res = sendReceive(tokens);
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
        		imageDir = "receivedImages/" + tokens[1] + "_" + user + "." + extension;
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
        
        String res = sendReceive(tokens);
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
        
        String res = sendReceive(tokens);
        
        //If everything went ok then the server will answer with a success message, otherwise, the error message.
        System.out.println(res);
        System.out.println();
    }
    
    public void read() {
        String res = "";
        try {
            this.out.writeObject("read");
            res = (String) this.in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        //If everything went ok then the server will answer with the user's messages, otherwise
        //it will say that there are no messages.
        System.out.println(res);
        System.out.println();
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
