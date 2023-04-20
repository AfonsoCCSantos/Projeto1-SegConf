package logs;

public abstract class Transaction {

	protected String vinhoId;
	protected String unidades;
	protected String valor;
	protected String user;
	
	public Transaction(String vinhoId, String unidades, String valor, String user) {
		this.vinhoId = vinhoId;
		this.unidades = unidades;
		this.valor = valor;
		this.user = user;
	}
	
	public abstract String toString();
	
	
	
	
}
