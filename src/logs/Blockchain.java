package logs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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

public class Blockchain {
	private static final String SERVER_FILES_BLOCKCHAIN = "serverFiles/blockchain";
	private static final int N_TRANSACTIONS_LINE = 2;
	private static final String SEPARATOR = "=";
	private static Blockchain INSTANCE = null;
	private File currentBlock;
	private int currentNumTransactions;
	private int currentBlockId = 0;
	private PrivateKey privateKey;
	private PublicKey publicKey;

	private Blockchain() {
		//Find latest block
		currentBlock = null;
		File blockchainFolder = new File(SERVER_FILES_BLOCKCHAIN);
		String[] blockchainFileNames = blockchainFolder.list();
				
		if(blockchainFileNames.length == 0) {
			currentBlock = new File(SERVER_FILES_BLOCKCHAIN + "block_1.blk");
			try {
				currentBlock.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
			createNewBlock();
		}
		else {
			String currBlockFileName = Collections.max(Arrays.asList(blockchainFolder.list()));
			currentBlock = new File(currBlockFileName);
		}
	}
	
	
	public void writeTransaction(Transaction transaction) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentBlock, true))) {
			writer.append(transaction.toString() + "\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.currentNumTransactions++;
		updateNumberOfTransactions(currentBlock, this.currentNumTransactions);
		if(currentNumTransactions == 5) {
			createNewBlock();
		}
	}
	
	
	
	//@requires verifiyIntegrityOfBlockchain
	public void load(PrivateKey privKey, PublicKey pubKey) {
		try (BufferedReader reader = new BufferedReader(new FileReader(currentBlock))) {
			reader.readLine();
			String[] idLineTokens= reader.readLine().split(SEPARATOR);
			this.currentBlockId = Integer.parseInt(idLineTokens[1]);
			String[] nTrxLineTokens= reader.readLine().split(SEPARATOR);
			this.currentNumTransactions = Integer.parseInt(nTrxLineTokens[1]);
			this.privateKey = privKey;
			this.publicKey = pubKey;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
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
		
		if (currentBlockId >= 1) {
			//assinar bloco fica no curr
			Signature s = null;
			byte[] buf;
			byte[] blockSignature = null;
			try (InputStream reader = new ObjectInputStream(new FileInputStream(currentBlock))) {
				s = Signature.getInstance("MD5withRSA");
				s.initSign(privateKey);
				buf = reader.readAllBytes();
				s.update(buf);
				blockSignature = s.sign();
			} catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
				e.printStackTrace();
			}		
				
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentBlock, true))){
				writer.append( blockSignature + "\n"); //escrever assinatura			
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//obter hash do bloco
			MessageDigest md = null;
			try (InputStream reader = new ObjectInputStream(new FileInputStream(currentBlock))) {
				md = MessageDigest.getInstance("SHA-256");
				buf = reader.readAllBytes();
				hash = md.digest(buf);
			} catch (IOException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		
		currentBlockId++;
		currentBlock = new File(SERVER_FILES_BLOCKCHAIN + "/block_" + this.currentBlockId + ".blk");
		
		//create new block
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentBlock, true))){
			writer.append( hash + "\n"); //escrever hash
			writer.append("blk_id"+ SEPARATOR +currentBlockId + "\n");
			writer.append("n_trx"+ SEPARATOR +this.currentNumTransactions + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		currentNumTransactions = 0;
	}
	
	public void updateNumberOfTransactions(File currentBlock, int numOfTransactions) {
		StringBuilder sb = new StringBuilder();
		String line = null;
		int currentLine = 0;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(currentBlock))) {
			line = reader.readLine();
			while (line != null) {
				if (currentLine == N_TRANSACTIONS_LINE) {
					sb.append(numOfTransactions);
				}
				else {
					sb.append("n_trx" + SEPARATOR + numOfTransactions + "\n");
				}
				currentLine++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentBlock))) {
			writer.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public boolean verifyIntegrityOfBlockchain() {
		String[] blockchainFileNames = new File(SERVER_FILES_BLOCKCHAIN).list();
		Arrays.sort(blockchainFileNames);
		String lastBlockFileName = SERVER_FILES_BLOCKCHAIN + "/" + currentBlock.getName();
		
		byte[] calculatedHash = new byte[32];
		for (String block : blockchainFileNames) {
			MessageDigest md = null;
			Signature s = null;
			
			try {
				s = Signature.getInstance("MD5withRSA");
				s.initVerify(this.publicKey);
				md = MessageDigest.getInstance("SHA-256");
				
				String hashRead = null; 
				String numTransactionsRead;
				String signatureRead = null;
				byte[] blockWithoutSignature = null; 
				byte[] blockWithSignature = null;
				
				
								
				try (BufferedReader reader = new BufferedReader(new FileReader(block))) {
					
					hashRead = reader.readLine();// TODO:pode ter de ser readAllByes()
					
					if (block.equals(lastBlockFileName)) {
						if (!MessageDigest.isEqual(calculatedHash, hashRead.getBytes())) {
							return false;
						}
						break;
					}
					
					String blockIdRead = reader.readLine();
					numTransactionsRead = reader.readLine();
					StringBuilder sb = new StringBuilder(hashRead + "\n" + blockIdRead +
							"\n" + numTransactionsRead + "\n");
					
					String line = null;
					int numCurrTransactions = 0;
					
					//Reading transactions lines
					while (true) {
						line = reader.readLine(); 
						
					 	if (numCurrTransactions == 5) {
							 blockWithoutSignature = sb.toString().getBytes();
							 signatureRead = line;
							 sb.append(line + "\n");
							 break;
						}
						else {
						 	numCurrTransactions++; 
						 	sb.append(line + "\n");
						} 
					}// depois nao ha translator
					
					blockWithSignature = sb.toString().getBytes(); 
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				s.update(blockWithoutSignature);
				
				if (!s.verify(signatureRead.getBytes()) ||
						!MessageDigest.isEqual(calculatedHash, hashRead.getBytes())) {
					return false;
				}
				calculatedHash = md.digest(blockWithSignature);
				
				
			} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
}
