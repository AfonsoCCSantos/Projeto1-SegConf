package logs;

import java.io.Serializable;

public abstract class Transaction implements Serializable {


	private static final long serialVersionUID = 1L;
	
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

	public String getVinhoId() {
		return vinhoId;
	}

	public void setVinhoId(String vinhoId) {
		this.vinhoId = vinhoId;
	}

	public String getUnidades() {
		return unidades;
	}

	public void setUnidades(String unidades) {
		this.unidades = unidades;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}
}
