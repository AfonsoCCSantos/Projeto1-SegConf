import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Random;

import javax.crypto.SecretKey;

import Catalogos.CatalogoDeMensagens;
import Catalogos.CatalogoDeSaldos;
import Catalogos.CatalogoDeUtilizadores;
import Catalogos.CatalogoDeVinhos;
import Catalogos.CatalogoVendas;
import logs.Blockchain;
import logs.BuyTransaction;
import logs.SellTransaction;


/**
 *
 * @author Afonso Santos - FC56368
 * @author Alexandre Figueiredo - FC57099
 * @author Raquel Domingos - FC56378
 *
 */
public class TintolSkel {
    
    private static final String FAILED_LOGIN_MESSAGE = "Log in failed";
    private static final String SUCCESS_LOGIN_MESSAGE = "Log in succeeded";
	
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private CatalogoDeUtilizadores catUsers;
    private CatalogoDeVinhos catWines;
    private CatalogoVendas catVendas;
    private CatalogoDeMensagens catMessages;
    private CatalogoDeSaldos catSaldos;
    private Blockchain blockchain;
	private SecretKey passwordKey;
	private Random rd;
    private Key privateKey;

    public TintolSkel(Socket inSocket, SecretKey passwordKey, Key privateKey) {
        this.in = Utils.gInputStream(inSocket);
        this.out = Utils.gOutputStream(inSocket);
        this.catUsers = CatalogoDeUtilizadores.getInstance();
        this.catWines = CatalogoDeVinhos.getInstance();
        this.catVendas = CatalogoVendas.getInstance();
        this.catMessages = CatalogoDeMensagens.getInstance();
        this.blockchain = Blockchain.getInstance();
        this.catSaldos = CatalogoDeSaldos.getInstance();
		this.passwordKey = passwordKey;
		this.rd = new Random();
		this.passwordKey = passwordKey;
		this.privateKey = privateKey;
   		
    }

    public String loginUser() {
        String user = null;
        Long nonce = this.rd.nextLong();
                
        try {
            user = (String) in.readObject();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        boolean userExists = this.catUsers.userExists(user);
        
        try {
			out.writeObject(nonce);
	        out.writeObject(userExists); 
		} catch (IOException e) {
			e.printStackTrace();
		} 
        
        if (userExists) {
	        String certificateFileName  = this.catUsers.getCertificateFileName(user);
	        PublicKey userPublicKey = null;
	        FileInputStream fis;
			try {
				fis = new FileInputStream("serverFiles/" + certificateFileName);
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				Certificate userCertificate = cf.generateCertificate(fis);
				userPublicKey = userCertificate.getPublicKey();
			} catch (FileNotFoundException | CertificateException e) {
				e.printStackTrace();
			}
	  			
			byte[] signedNonce;
			try {
				signedNonce = (byte[]) in.readObject();
				Signature s = Signature.getInstance("MD5withRSA");
				s.initVerify(userPublicKey);
				s.update(nonce.byteValue());
				if(s.verify(signedNonce)) {
	                out.writeObject(SUCCESS_LOGIN_MESSAGE);
					out.writeObject(true);
	                return user;
	            }
				out.writeObject(FAILED_LOGIN_MESSAGE);
	            out.writeObject(false);
	            out.close();
	            in.close();
			} catch (ClassNotFoundException | IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
				e.printStackTrace();
			}
            return null;     
        }
        //user doesnt exist
        else {
			try {
				Long receivedNonce = (Long) in.readObject();
				byte[] receivedSignedNonce = (byte[]) in.readObject();
	        	Certificate receivedCertificate = (Certificate) in.readObject(); 
	        	PublicKey usersPublicKey = receivedCertificate.getPublicKey();
	        	Signature s = Signature.getInstance("MD5withRSA");
				s.initVerify(usersPublicKey);
				s.update(nonce.byteValue());
	        	boolean isTheSameNonce = receivedNonce.equals(nonce);
	        	boolean isTheNonceSigned = s.verify(receivedSignedNonce);
	        	
	        	System.out.println(isTheSameNonce);
	        	System.out.println(isTheNonceSigned);
	        	
	        	if (isTheSameNonce && isTheNonceSigned) {
	                String certificateFileName =user + ".cer";
	        		byte[] buf = receivedCertificate.getEncoded();
	        		FileOutputStream os = new FileOutputStream("serverFiles/" + certificateFileName);
	        		os.write(buf);
	        		os.close();	
	        		this.catUsers.registerUser(user,certificateFileName);
	        		this.catSaldos.registerUser(user);
	        		out.writeObject(SUCCESS_LOGIN_MESSAGE);
	        		out.writeObject(true);
	        		return user;
	        	}
	        	else {
	        		out.writeObject(FAILED_LOGIN_MESSAGE);
	        		out.writeObject(false);
	        	}
			} catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | SignatureException | InvalidKeyException | CertificateEncodingException e) {
				e.printStackTrace();
			}	
        }
        return null;
    }

    public void addWine(String wine, String imagePath) {
    	boolean res = false;
    	
    	synchronized (this.catWines) {
    		res = this.catWines.registerWine(wine, imagePath);
    	}

    	if (!res) {
            try {
				out.writeObject(false);
				out.writeObject("The wine already exists.");
			} catch (IOException e) {
				e.printStackTrace();
			}
            return;
        }

    	try {
            this.out.writeObject(true);
			byte[] fileContent = (byte[]) this.in.readObject();
			FileOutputStream fos = new FileOutputStream(new File(imagePath));
			fos.write(fileContent);
			fos.close();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

    	try {
			out.writeObject("The wine has been added.");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void sellWine(String user, String wine, String value, String quantity) {

    	synchronized (this.catWines) {
    		if (!this.catWines.wineExists(wine)) {
        		try {
    				out.writeObject("This wine does not exist.");
    				return;
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
        	}
    	}

    	synchronized (this.catVendas) {
            this.catVendas.sellWine(wine, user, value, quantity);
        }

        synchronized (this.catWines) {
            this.catWines.updateQuantity(wine, Integer.parseInt(quantity));
        }
        
        synchronized (this.blockchain) {
        	this.blockchain.writeTransaction(new SellTransaction(wine, quantity, value, user));
		}

        try {
			out.writeObject("This wine is now for sell.");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void view(String wine) {
        String wineInfo = "Models.Wine information:\n";
        File f = null;
        synchronized (this.catWines) {
	        if (!this.catWines.wineExists(wine)) {
	    		try {
	    			out.writeObject(false);
					out.writeObject("This wine does not exist.");
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}

	        try {
				out.writeObject(true); //tells the client that the wine exists and image will be sent
			} catch (IOException e) {
				e.printStackTrace();
			}

	    	wineInfo = catWines.getWineInfo(wine);
	    	f = new File(catWines.getWine(wine).getImage());
    	}

        String extension = Utils.getFileExtension(catWines.getWine(wine).getImage());
        FileInputStream fin = null;
        try {
        	out.writeObject(extension);
        	fin = new FileInputStream(f);
        	InputStream input = new BufferedInputStream(fin);
        	byte[] buffer = new byte[(int) f.length()];
        	input.read(buffer);

        	out.writeObject(buffer);

        	fin.close();
        	input.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }

    	synchronized (this.catVendas) {
            wineInfo = wineInfo.concat("\n" + catVendas.getWineSalesInfo(wine));
        }

    	try {
			out.writeObject(wineInfo);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void buy(String user, String wine, String seller, String quantity) {
    	//1. Verificar se o vinho existe
		if (!this.catWines.wineExists(wine)) {
    		try {
				out.writeObject("This wine does not exist.");
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}

		double winePrice = 0;

		synchronized(this.catVendas){
	    	if (!this.catVendas.userSellsWine(seller, wine)) {
	    		try {
					out.writeObject("The given user is not selling this wine");
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    	//2.Obter o valor que o user ira pagar ao comprar estas unidades do dado vinho
	    	winePrice = this.catVendas.getWinePrice(wine,seller) * Integer.parseInt(quantity);
        }

    	//3.Verificar se ha quantidades suficientes do vinho
    	synchronized (this.catWines) {
    		if (!this.catWines.hasEnoughQuantity(wine, Integer.parseInt(quantity))) {
    			try {
					out.writeObject("There are not enought units of this wine.");
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}
    	//4. Verificar se user tem dinheiro suficiente
    	synchronized(this.catSaldos) {
            if (this.catSaldos.userHasMoney(user,winePrice)) {
	    	//4.1. Tirar da conta do user que compra - Se nao tiver dinheiro suficiente, avisar
				this.catSaldos.updateMoney(user,-winePrice);
    		//4.2. Por o dinheiro na conta do user que vende
				this.catSaldos.updateMoney(seller,winePrice);
            }
            else {
            	try {
					out.writeObject("You do not have enough money to make this purchase.");
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }

        //5. tirar a sell ou a quantidade de vinho comparada da sell
        synchronized(this.catVendas){
            this.catVendas.removeQuantityFromSell(wine,seller,quantity);
        }

		//6. tirar a quantidade de vinho
    	synchronized (this.catWines) {
    		this.catWines.updateQuantity(wine, -Integer.parseInt(quantity));
    	}
    	
    	 synchronized (this.blockchain) {
         	this.blockchain.writeTransaction(new BuyTransaction(wine, quantity, String.valueOf(winePrice), user));
 		}

    	try {
			out.writeObject("The wine was bought.");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void wallet(String user) {
    	double userMoney = catSaldos.getUserMoney(user);
    	try {
			out.writeObject("Your balance is: " + userMoney);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void classify(String wine, String stars) {
    	if (!this.catWines.wineExists(wine)) {
    		try {
				out.writeObject("This wine does not exist.");
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	synchronized(this.catWines) {
    		this.catWines.classifyWine(wine, Integer.parseInt(stars));
    		try {
				out.writeObject("The wine was rated");
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

    public void talk(String fromUser, String toUser, String message) {
    	if (!this.catUsers.userExists(toUser)) {
    		try {
				out.writeObject("The user does not exist.");
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    	synchronized (this.catMessages) {
    		this.catMessages.registerMessage(toUser, fromUser, message);
    	}

   		try {
			out.writeObject("Models.Message sent successfully.");
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public void read(String user) {
    	String messages = null;
    	synchronized (this.catMessages) {
    		messages = this.catMessages.readMessages(user);
    	}
    	try {
			out.writeObject(messages);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    public String getLine() {
        String line = null;
        try {
            line = (String) in.readObject();
        } catch(IOException | ClassNotFoundException e) {
        }
        return line;
    }

}
