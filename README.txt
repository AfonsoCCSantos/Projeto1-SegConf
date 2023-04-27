Para compilar o jar do servidor: jar cvfm TintolmarketServer.jar manifestTintolmarketServer.txt TintolmarketServer.class ServerThread.class TintolSkel.class Catalogos/Catalogo.class Catalogos/CatalogoDeMensagens.class Catalogos/CatalogoDeSaldos.class Catalogos/CatalogoDeUtilizadores.class Catalogos/CatalogoDeVinhos.class Catalogos/CatalogoVendas.class Models/Message.class Utils.class ValidationLib.class Models/Wine.class Models/WineSell.class logs/Blockchain.class logs/Bloco.class logs/BuyTransaction.class logs/Hmac.class logs/SellTransaction.class logs/Transaction.class

Para compilar o jar do cliente: jar cvfm Tintolmarket.jar manifestTintolmarket.txt Tintolmarket.class TintolStub.class Utils.class ValidationLib.class logs/BuyTransaction.class logs/SellTransaction.class logs/Transaction.class

Foram tambem enviados os ficheiros manifestTintolmarketServer.txt e manifestTintolmarket.txt

Para compilar TintolMarket: javac Tintolmarket.java
Para compilar TintolMarketServer: javac TintolmarketServer.java

Para executar o servidor: java -jar TintolmarketServer.jar porto(opcional) password-cifra keystore password-keystore 
	(Caso nao seja colocado o porto nos argumentos, o socket ira ser criada com o porto 12345)

Para executar o cliente: java -jar Tintolmarket.jar localhost:portoDoServidor(opcional) truststore keystore password-keystore userId 
	(Caso nao seja colocado o porto, ira tentar ligar-se ao porto 12345)


Para terminar ambos os programas basta fazer CTRL C


Os ficheiros no servidor sao guardados da seguinte forma:
	-Existe uma pasta chamada "serverFiles", dentro desta eh criada uma pasta "images"  e uma pasta "blockchain".
	-Dentro de "serverFiles/images" serao guardadas as imagens enviadas pelos clientes aquando da sua operacao "addWine"
	-Essas imagens serao guardadas com o nome "nomeDoVinho_server.extensao" 
	-Dentro de "serverFiles/blockchain" serao guardados os ficheiros .blk da blockchain.
	-Dentro de "serverFiles" sao tambem guardados os ficheiros "messages.txt", "sells.txt", "users.cif", "wines.txt", "budgets.txt", "params.txt", "macs.txt" e os certificados dos users.
	-Em "messages.txt" sao guardadas as mensagens entre os utilizadores, cada utilizador tem uma linha onde serao
	 guardadas as suas mensagens. As mesmas terao o seguinte formato:
	 	utilizadorDestinatario-utilizadorQueEnvia1:<msg1_cifrada>;utilizadorQueEnvia2:<msg2_cifrada>
	-Em "sells.txt" sao guardadas as informacoes relativas as vendas dos vinhos, como o nome do vendedor, o nome do vinho
	 o preco por unidade e o numero de unidades que estao ah venda. As linhas no ficheiro terao o seguinte formato:
	 	nomeDoVendedor:nomeDoVinho:preco:unidadesDisponiveis
    -Em "wines.txt" sao guardadas as informacoes dos vinhos, sendo estas o nome do vinho, o path da imagem do vinho no servidor,
    a soma de todas as classificacoes recebidas, o numero de classificacoes recebidas e a quantidade de unidades existentes do vinho.
    A soma de todas as classificacoes e o numero de classificacoes recebidas sao usadas para calcular a classificacao media do vinho.
    Cada linha representa um vinho e as linhas do ficheiro teem o seguinte formato:
    	nomDoVinho:pathDaImagem:somaClassificacoes:numeroDeClassificacoes:unidadesDoVinho
    -Em "users.cif" sao guardados de forma cifrada os utilizadores registados no servidor. Cada linha  representa um utilizador registado,
     indicando o nome e o path do seu certifado. As linhas teem o seguinte formato:
     	utilizador:pathDoCertificado
     -Em "budgets.txt" sao guardados os saldos dos utilizadores registados. Cada linha representa um utilizador registado
      e o seu saldo atual. O formato das linhas sao:
      	utilizador:dinheiroDisponivel
     -Em "params.txt" sao guardados os parametros utilizados para cifrar e decifrar o ficheiro "users.cif"
     -Em "macs.txt" eh guardado o Hmac do conteudo dos ficheiros "messages.txt", "sells.txt", "wines.txt", "budgets.txt" 	

As imagens enviadas para o cliente aquando da operacao "view" sao guardadas da seguinte forma:
	-Quando a imagem eh recebida, a mesma eh guardada com o nome: nomeDoVinho_nomeDoUser.extensao


So sao aceites imagens com extensoes png, jpg ou jpeg.

Os nomes dos vinhos nao podem conter qualquer um dos seguintes caracteres: "(){}[]|`! \"$%^&*<>:;#~_-+=,@."
