package logs;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedOutputStream;
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
	public void load() {
		try (BufferedReader reader = new BufferedReader(new FileReader(currentBlock))) {
			reader.readLine();
			String[] idLineTokens = reader.readLine().split(SEPARATOR);
			this.currentBlockId = Integer.parseInt(idLineTokens[1]);
			String[] nTrxLineTokens= reader.readLine().split(SEPARATOR);
			this.currentNumTransactions = Integer.parseInt(nTrxLineTokens[1]);
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
	
	public String readAllLines(BufferedReader reader){
		String lineRead = null;
		StringBuilder sb = new StringBuilder();
		try {
			lineRead = reader.readLine();
			while(lineRead != null){
				sb.append(lineRead + "\n");
				lineRead = reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	public void createNewBlock() {
		byte[] hash = new byte[32];
		
		if (currentBlockId >= 1) {
			//assinar bloco fica no curr
			Signature s = null;
			byte[] blockSignature = null;
			StringBuilder allLines = null;
			try (BufferedReader reader = new BufferedReader(new FileReader(currentBlock))) {
				s = Signature.getInstance("MD5withRSA");
				s.initSign(privateKey);
				allLines = new StringBuilder(readAllLines(reader));
				
				s.update(allLines.toString().getBytes());
				blockSignature = s.sign();
			} catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
				e.printStackTrace();
			}		
			
			String signatureLine = new String(blockSignature) + "\n";
			allLines.append(signatureLine);
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentBlock, true))){
				writer.append(signatureLine); //escrever assinatura			
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			//obter hash do bloco
			MessageDigest md = null;
			try {
				md = MessageDigest.getInstance("SHA-256");
				hash = md.digest(allLines.toString().getBytes());
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
		
		currentBlockId++;
		currentBlock = new File(SERVER_FILES_BLOCKCHAIN + "/block_" + this.currentBlockId + ".blk");
		
		//create new block
		this.currentNumTransactions = 0;
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentBlock, true))) {
			writer.append(new String(hash) + "\n");
			writer.append("blk_id"+ SEPARATOR + currentBlockId + "\n");
			writer.append("n_trx"+ SEPARATOR +this.currentNumTransactions + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void updateNumberOfTransactions(File currentBlock, int numOfTransactions) {
		StringBuilder sb = new StringBuilder();
		String line = null;
		int currentLine = 0;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(currentBlock))) {
			line = reader.readLine();
			while (line != null) {
				if (currentLine == N_TRANSACTIONS_LINE) {
					sb.append("n_trx" + SEPARATOR + numOfTransactions + "\n");
				}
				else {
					sb.append(line + "\n");
				}
				currentLine++;
				line = reader.readLine();
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
		for (String blockFileName : blockchainFileNames) {
			MessageDigest md = null;
			Signature s = null;
		    blockFileName = SERVER_FILES_BLOCKCHAIN + "/" + blockFileName;
			
			try {
				s = Signature.getInstance("MD5withRSA");
				s.initVerify(this.publicKey);
				md = MessageDigest.getInstance("SHA-256");
				
				String hashRead = null; 
				String numTransactionsRead;
				byte[] blockWithoutSignature = null; 
				byte[] blockWithSignature = null;
				StringBuilder signatureRead = new StringBuilder();
				
				try (BufferedReader reader = new BufferedReader(new FileReader(blockFileName))) {
					hashRead = reader.readLine();// TODO:pode ter de ser readAllByes()
					if (blockFileName.equals(lastBlockFileName)) {
//						if (!MessageDigest.isEqual(calculatedHash, hashRead.getBytes())) {
//							return false;
//						}
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
							 while(line != null) {
								 signatureRead.append(line + "\n");
								 line = reader.readLine();
							 }
							 signatureRead.deleteCharAt(signatureRead.length()-1);
							 sb.append(signatureRead.toString() + "\n");
							 break;
						}
						else {
						 	numCurrTransactions++; 
						 	sb.append(line + "\n");
						} 
					}
					
					blockWithSignature = sb.toString().getBytes(); 
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				s.update(blockWithoutSignature);
				
				//TODO
				//Verificar, nao funciona a verificacao da assinatura
//				if (!s.verify(signatureRead.toString().getBytes()) ||
//						!MessageDigest.isEqual(calculatedHash, hashRead.getBytes())) {
//					return false;
//				}
				calculatedHash = md.digest(blockWithSignature);
				
			} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}
	
}
