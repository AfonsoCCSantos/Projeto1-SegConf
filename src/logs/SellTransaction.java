package logs;

/**
*
* @author Afonso Santos - FC56368
* @author Alexandre Figueiredo - FC57099
* @author Raquel Domingos - FC56378
*
*/
public class SellTransaction extends Transaction {


	private static final long serialVersionUID = 1L;

	public SellTransaction(String vinhoId, String unidades, String valor, String user) {
		super(vinhoId, unidades, valor, user);
	}

	@Override
	public String toString() {
		return "Sell: id do vinho: " + this.vinhoId + " ; unidades criadas: " + this.unidades +
				" ; valor de cada unidade: " + this.valor + " ; vendedor: " + this.user;
	}

}
