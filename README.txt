Para compilar o jar do servidor: jar cvfm TintolmarketServer.jar manifestTintolmarketServer.txt bin/TintolmarketServer.class bin/ServerThread.class bin/TintolSkel.class bin/Catalogos/Catalogo.class bin/Catalogos/CatalogoDeMensagens.class bin/Catalogos/CatalogoDeSaldos.class bin/Catalogos/CatalogoDeUtilizadores.class bin/Catalogos/CatalogoDeVinhos.class bin/Catalogos/CatalogoVendas.class bin/Models/Message.class bin/Utils.class bin/ValidationLib.class bin/Models/Wine.class bin/Models/WineSell.class bin/logs/Blockchain.class bin/logs/Bloco.class bin/logs/BuyTransaction.class bin/logs/Hmac.class bin/logs/SellTransaction.class bin/logs/Transaction.class

Para compilar o jar do cliente: jar cvfm Tintolmarket.jar manifestTintolmarket.txt bin/Tintolmarket.class bin/TintolStub.class bin/Utils.class bin/ValidationLib.class bin/logs/BuyTransaction.class bin/logs/SellTransaction.class bin/logs/Transaction.class

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
     - A keystore.server e o certificado do servidor tambem esta incluida na pasta /serverFiles
     - Quando eh registado um novo cliente, eh guardado o seu certificado tambem na pasta serverFiles/ 

As imagens enviadas para o cliente aquando da operacao "view" sao guardadas da seguinte forma:
	-Quando a imagem eh recebida, a mesma eh guardada com o nome: nomeDoVinho_nomeDoUser.extensao e colocada na pasta /userFiles.


So sao aceites imagens com extensoes png, jpg ou jpeg.

Os nomes dos vinhos nao podem conter qualquer um dos seguintes caracteres: "(){}[]|`! \"$%^&*<>:;#~_-+=,@."

Na pasta /userFiles estao:
	- keystore.client1 e keystore.client2 (keystores dos clientes)
	- truststore.client (truststore com os certificados de todos os clientes e do servidor)
	- as imagens que os clientes recebem ao executar comando "view"

Antes de ser adicionado um novo cliente, na pasta /userFiles, deve ser:
	- adicionada um nova keystore para o mesmo: keytool -genkeypair -alias <userId> -keyalg RSA -keysize 2048 -keystore keystore.userId
	- exportar o certificado criado: keytool -exportcert -alias <userId> -file certUserId.cer -keystore keystore.userId
	- importar o certificado criado para a truststore: keytool -importcert -alias <userId> -file certUserId.cer -keystore truststore.client
	
Para ver os conteudos de uma keystore: keytool -list -keystore <keystore>
Para ver o conteudo de um certificado: keytool -printcert -file <certificado>	
	 	