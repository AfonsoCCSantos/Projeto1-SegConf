package logs;

/**
*
* @author Afonso Santos - FC56368
* @author Alexandre Figueiredo - FC57099
* @author Raquel Domingos - FC56378
*
*/
public class BuyTransaction extends Transaction {

	private static final long serialVersionUID = 1L;

	public BuyTransaction(String vinhoId, String unidades, String valor, String user) {
		super(vinhoId, unidades, valor, user);
	}

	@Override
	public String toString() {
		return "Buy: id do vinho: " + this.vinhoId + " ; unidades vendidas: " + this.unidades +
				" ; valor de cada unidade: " + this.valor + " ; comprador: " + this.user;
	}

}
