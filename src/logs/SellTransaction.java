package logs;

public class SellTransaction extends Transaction {

	public SellTransaction(String vinhoId, String unidades, String valor, String user) {
		super(vinhoId, unidades, valor, user);
	}

	@Override
	public String toString() {
		return "Sell: id do vinho: " + this.vinhoId + " ; unidades criadas: " + this.unidades +
				" ; valor de cada unidade: " + this.valor + " ; vendedor: " + this.user;
	}

}
