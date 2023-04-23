package logs;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.security.SignedObject;
import java.util.ArrayList;

public class Blockchain {
	private static final String SERVER_FILES_BLOCKCHAIN = "serverFiles/blockchain";
	private static Blockchain INSTANCE = null;
	private File currentBlock;
	private long currentNumTransactions;
	private long currentBlockId = 0;
	private PrivateKey privateKey;
	private PublicKey publicKey;
	private List<String> transactions;

	private Blockchain() {
		//Find latest block
		currentBlock = null;
		File blockchainFolder = new File(SERVER_FILES_BLOCKCHAIN);
		String[] blockchainFileNames = blockchainFolder.list();
				
		if(blockchainFileNames.length == 0) {
			currentBlock = new File(SERVER_FILES_BLOCKCHAIN + "/block_1.blk");
			try {
				currentBlock.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			createNewBlock();
		}
		else {
			String currBlockFileName = Collections.max(Arrays.asList(blockchainFolder.list()));
			currentBlock = new File(SERVER_FILES_BLOCKCHAIN + "/" + currBlockFileName);
		}
		this.transactions = new ArrayList<>();
	}
	
	
	public void writeTransaction(Transaction transaction) {
		Bloco toUpdate = null;
		try {
			FileInputStream fis = new FileInputStream(currentBlock);
			ObjectInputStream ois = new ObjectInputStream(fis);
			toUpdate = (Bloco) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		try {
			FileOutputStream fos = new FileOutputStream(currentBlock);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			toUpdate.getTransactions().add(transaction.toString());
			oos.writeObject(toUpdate);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.currentNumTransactions++;
		updateNumberOfTransactions(currentBlock, this.currentNumTransactions);
		
		if(currentNumTransactions == 5) {
			createNewBlock();
		}
		this.transactions.add(transaction.toString());
	}
	
	//@requires verifiyIntegrityOfBlockchain
	public void load() {
		try {
			FileInputStream fis = new FileInputStream(currentBlock);
			ObjectInputStream ois = new ObjectInputStream(fis);
			Bloco block = (Bloco) ois.readObject();
			this.currentBlockId = block.getBlockId();
			this.currentNumTransactions = block.getNumOfTransactions();
			ois.close();
			
			File blockchainFolder = new File(SERVER_FILES_BLOCKCHAIN);
			String[] blockchainFileNames = blockchainFolder.list();
			Arrays.sort(blockchainFileNames);
			
			for(String blockFileName : blockchainFileNames) {
				blockFileName = SERVER_FILES_BLOCKCHAIN + "/" + blockFileName;
				fis = new FileInputStream(blockFileName);
				ois = new ObjectInputStream(fis);
				String fullName = SERVER_FILES_BLOCKCHAIN + "/" + currentBlock.getName();
				if(blockFileName.equals(fullName)) {
					block = (Bloco) ois.readObject();
				}
				else {
					SignedObject toCheckSignature = (SignedObject) ois.readObject();
					block = (Bloco) toCheckSignature.getObject();	
				}
				for (String transaction : block.getTransactions()) {
					this.transactions.add(transaction);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	

	public static Blockchain getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new Blockchain();
		}
		return INSTANCE;
	}
	
	public void createNewBlock() {
		byte[] hash = new byte[32];
		Bloco block = null;
		currentBlockId++;
		if (currentBlockId > 1) {
			try {
				FileInputStream fis = new FileInputStream(currentBlock);
				ObjectInputStream ois = new ObjectInputStream(fis);
				block = (Bloco) ois.readObject();
				ois.close();
				fis.close();
				
				Signature s = Signature.getInstance("MD5withRSA");
				SignedObject signedBlock = new SignedObject(block, this.privateKey, s);
				
				FileOutputStream fos = new FileOutputStream(currentBlock);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(signedBlock);
				oos.close();
				fos.close();
				//Here the last block was sealed, lets get the hash
				
				//1st Serialise the object to byte array
				ByteArrayOutputStream out = new ByteArrayOutputStream();
			    ObjectOutputStream os = new ObjectOutputStream(out);
			    os.writeObject(signedBlock);
			    byte[] bytes = out.toByteArray();
			    os.close();
			    out.close();
				
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				hash = md.digest(bytes); //This will serve as the hash for the new block
				this.currentNumTransactions = 0;
			} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		currentBlock = new File(SERVER_FILES_BLOCKCHAIN + "/block_" + this.currentBlockId + ".blk");
		block = new Bloco(hash, currentBlockId, 0);
		try {
			FileOutputStream fos = new FileOutputStream(currentBlock);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(block);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void updateNumberOfTransactions(File currentBlock, long numOfTransactions) {
		Bloco toUpdate = null;
		try {
			FileInputStream fis = new FileInputStream(currentBlock);
			ObjectInputStream ois = new ObjectInputStream(fis);
			toUpdate = (Bloco) ois.readObject();
			ois.close();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

		try {
			FileOutputStream fos = new FileOutputStream(currentBlock);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			toUpdate.setNumOfTransactions(numOfTransactions);
			oos.writeObject(toUpdate);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean verifyIntegrityOfBlockchain() {
		String[] blockchainFileNames = new File(SERVER_FILES_BLOCKCHAIN).list();
		byte[] previousBlockHash = new byte[32];
		
		Arrays.sort(blockchainFileNames);
		String lastBlockFileName = SERVER_FILES_BLOCKCHAIN + "/" + currentBlock.getName();
		
		for (String blockFileName : blockchainFileNames) {
			blockFileName = SERVER_FILES_BLOCKCHAIN + "/" + blockFileName;
			try {
				FileInputStream fis = new FileInputStream(blockFileName);
				ObjectInputStream ois = new ObjectInputStream(fis);
				if (blockFileName.equals(lastBlockFileName)) {
					Bloco block = (Bloco) ois.readObject();
					ois.close();
					fis.close();
					return MessageDigest.isEqual(previousBlockHash, block.getHash());
				}
				SignedObject toCheckSignature = (SignedObject) ois.readObject();
				ois.close();
				Signature s = Signature.getInstance("MD5withRSA");
				Bloco savedObject = (Bloco) toCheckSignature.getObject();
				
				if (!toCheckSignature.verify(this.publicKey, s) || 
						!MessageDigest.isEqual(previousBlockHash, savedObject.getHash())) {
					return false;
				}
				
				//Update previousBlockHash
				ByteArrayOutputStream out = new ByteArrayOutputStream();
			    ObjectOutputStream os = new ObjectOutputStream(out);
			    os.writeObject(toCheckSignature);
			    byte[] bytes = out.toByteArray();
			    os.close();
			    out.close();
				
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				previousBlockHash = md.digest(bytes); //This will serve as the hash for the new block
			} catch (IOException | ClassNotFoundException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
	
	public String listTransactions() {
		StringBuilder sb = new StringBuilder();
		for (String transaction : this.transactions) {
			sb.append(transaction + "\n");
		}
		return sb.toString();
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	
	
	
}
