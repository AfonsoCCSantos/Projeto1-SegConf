package logs;

public class BuyTransaction extends Transaction {

	public BuyTransaction(String vinhoId, String unidades, String valor, String user) {
		super(vinhoId, unidades, valor, user);
	}

	@Override
	public String toString() {
		return "Buy: id do vinho: " + this.vinhoId + " ; unidades vendidas: " + this.unidades +
				" ; valor de cada unidade: " + this.valor + " ; comprador: " + this.user;
	}

}
