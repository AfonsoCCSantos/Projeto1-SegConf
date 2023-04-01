Para compilar o jar do servidor: jar cvfm TintolmarketServer.jar manifestTintolmarketServer.txt TintolmarketServer.class ServerThread.class TintolSkel.class Catalogo.class CatalogoDeMensagens.class CatalogoDeSaldos.class CatalogoDeUtilizadores.class CatalogoDeVinhos.class CatalogoVendas.class Message.class Utils.class ValidationLib.class Wine.class WineSell.class
						  
Para compilar o jar do cliente: jar cvfm Tintolmarket.jar manifestTintolmarket.txt Tintolmarket.class TintolStub.class Utils.class ValidationLib.class						  

Foram tambem enviados os ficheiros manifestTintolmarketServer.txt e manifestTintolmarket.txt

Para compilar TintolMarket: javac Tintolmarket.java
Para compilar TintolMarketServer: javac TintolmarketServer.java

Para executar o servidor: java -jar TintolmarketServer.jar porto(opcional)
	(Caso nao seja colocado o porto nos argumentos, o socket ira ser criada com o porto 12345)
	
Para executar o cliente: java -jar Tintolmarket.jar localhost:portoDoServidor(opcional) NomeDoUser password(opcional)
	(Caso nao seja colocada a password nos argumentos, a mesma sera pedida durante o programa, caso nao seja colocado o porto, ira tentar ligar-se ao porto 12345)


Para terminar ambos os programas basta fazer CTRL C 		
	
	
Os ficheiros no servidor são guardados da seguinte forma:
	-Se a mesma nao existe, eh criada uma pasta chamada "serverFiles", dentro desta eh criada uma pasta "images".
	-Dentro de "serverFiles/images" serao guardadas as imagens enviadas pelos clientes aquando da sua operacao "addWine"
	-Essas imagens serao guardadas com o nome "nomeDoVinho_server.extensao"
	-Dentro de "serverFiles" sao tambem guardados os ficheiros "messages.txt", "sells.txt", "users.txt", "wines.txt", "budgets.txt" 
	-Em "messages.txt" sao guardadas as mensagens entre os utilizadores, cada utilizador tem uma linha onde serao 
	 guardadas as suas mensagens. As mesmas terao o seguinte formato:
	 	utilizadorDestinatario-utilizadorQueEnvia1:msg1;utilizadorQueEnvia2:msg2
	-Em "sells.txt" sao guardadas as informacoes relativas as vendas dos vinhos, como o nome do vendedor, o nome do vinho
	 o preco por unidade e o numero de unidades que estao ah venda. As linhas no ficheiro terao o seguinte formato:
	 	nomeDoVendedor:nomeDoVinho:preco:unidadesDisponiveis  
    -Em "wines.txt" sao guardadas as informacoes dos vinhos, sendo estas o nome do vinho, o path da imagem do vinho no servidor,
    a soma de todas as classificacoes recebidas, o numero de classificacoes recebidas e a quantidade de unidades existentes do vinho.
    A soma de todas as classificacoes e o numero de classificacoes recebidas sao usadas para calcular a classificacao media do vinho.
    Cada linha representa um vinho e as linhas do ficheiro teem o seguinte formato:
    	nomDoVinho:pathDaImagem:somaClassificacoes:numeroDeClassificacoes:unidadesDoVinho
    -Em "users.txt são guardados os utilizadores registados no servidor. Cada linha  representa um utilizador registado,
     indicando o nome e a password. As linhas teem o seguinte formato:
     	utilizador:password
     -Em "budgets.txt" são guardados os saldos dos utilizadores registados. Cada linha representa um utilizador registado
      e o seu saldo atual. O formato das linhas é:
      	utilizador:dinheiroDisponivel
     	
As imagens enviadas para o cliente aquando da operacao "view" sao guardadas da seguinte forma:     
	-Se a mesma nao existe, eh criada uma pasta chamada "receivedImages".
	-Dentro da pasta "receivedImages" serao guardadas as imagens dos vinhos aos quais o cliente faz view.
	-Quando a imagem eh recebida, a mesma eh guardada com o nome: nomeDoVinho_nomeDoUser.extensao
	

So sao aceites imagens com extensoes png, jpg ou jpeg.	

Os nomes, tanto dos vinhos quanto dos utilizadores, nao podem conter qualquer um dos seguintes caracteres: "(){}[]|`! \"$%^&*<>:;#~_-+=,@."  
	