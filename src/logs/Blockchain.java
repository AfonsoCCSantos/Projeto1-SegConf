package logs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class Blockchain {
	private static final String SERVER_FILES_BLOCKCHAIN = "serverFiles/blockchain";
	private static final String SEPARATOR = "=";
	private static Blockchain INSTANCE = null;
	private File currentBlock;
	private int currentNumTransactions;
	private int currentBlockId = 0;

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
		}
		else {
			
			String currBlockFileName = Collections.max(Arrays.asList(blockchainFolder.list()));
			currentBlock = new File(currBlockFileName);
			createNewBlock();
		}
										
			
		
	}
	public void writeTransaction(Transaction transaction) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentBlock, true))){
			writer.append(transaction.toString() + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.currentNumTransactions++;
		if(currentNumTransactions == 5) {
			createNewBlock();
		}
	}
	
	public void load() {
		try (BufferedReader reader = new BufferedReader(new FileReader(currentBlock))) {
			reader.readLine();
			String[] idLineTokens= reader.readLine().split(SEPARATOR);
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
	
	public void createNewBlock() {
		byte[] hash = new byte[32];
		
		
		if(currentBlockId == 1) {
			//hash = 000000...

			
		}
		// 
		else {
			//assinar bloco ficq no curr
			//asiinar bloco com assinatura fica no next
			
		}
		
		currentBlockId++;
		currentBlock = new File(SERVER_FILES_BLOCKCHAIN + "/block_" + this.currentBlockId + ".blk");
		
		//create first block
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentBlock, true))){
			writer.append( "00000000" + "\n"); //escrever hash
			writer.append("blk_id"+ SEPARATOR +currentBlockId + "\n");
			writer.append("n_trx"+ SEPARATOR +this.currentNumTransactions + "\n");
			
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		
		
	}
}
