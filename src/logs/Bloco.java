package logs;

import java.io.Serializable;
import java.security.SignedObject;
import java.util.List;
import java.util.ArrayList;

public class Bloco implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private byte[] hash;
	
	private long blockId;
	
	private long numOfTransactions;
	
	private List<SignedObject> transactions;
	
	public Bloco(byte[] hash, long blockId, long numOfTransactions) {
		this.hash = hash;
		this.blockId = blockId;
		this.numOfTransactions = numOfTransactions;
		this.transactions = new ArrayList<SignedObject>();
	}
	
	public Bloco(byte[] hash, long blockId, long numOfTransactions, List<SignedObject> transactions) {
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

	public List<SignedObject> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<SignedObject> transactions) {
		this.transactions = transactions;
	}
	
}
