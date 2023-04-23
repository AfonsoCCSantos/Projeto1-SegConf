package logs;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Bloco implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private byte[] hash;
	
	private long blockId;
	
	private long numOfTransactions;
	
	private List<String> transactions;
	
	public Bloco(byte[] hash, long blockId, long numOfTransactions) {
		this.hash = hash;
		this.blockId = blockId;
		this.numOfTransactions = numOfTransactions;
		this.transactions = new ArrayList<String>();
	}
	
	public Bloco(byte[] hash, long blockId, long numOfTransactions, List<String> transactions) {
		this.hash = hash;
		this.blockId = blockId;
		this.numOfTransactions = numOfTransactions;
		this.transactions = transactions;
	}

	public byte[] getHash() {
		return hash;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}

	public long getBlockId() {
		return blockId;
	}

	public void setBlockId(long blockId) {
		this.blockId = blockId;
	}

	public long getNumOfTransactions() {
		return numOfTransactions;
	}

	public void setNumOfTransactions(long numOfTransactions) {
		this.numOfTransactions = numOfTransactions;
	}

	public List<String> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<String> transactions) {
		this.transactions = transactions;
	}
	
}
