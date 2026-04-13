# Trabalho 1 - Sistemas Distribuidos

Dupla: Francisco Mateus | Mateus Dodo

## Visao Geral

Sistema de chat em terminal via TCP, com comunicacao baseada em objetos serializados usando ObjectOutputStream e ObjectInputStream.

## Funcionalidades Atuais

- Registro de usuario ao conectar no servidor.
- Menu interativo no cliente via terminal.
- Envio de mensagem em broadcast para todos conectados.
- Listagem de usuarios conectados.
- Chat privado entre dois usuarios conectados.
- Listagem de grupos disponiveis.
- Entrada em grupos predefinidos.
- Envio de mensagens para grupo (apenas para membros).
- Saida de conversa privada ou grupo para o menu principal com os comandos: /voltar, voltar, sair ou 0.

## Grupos Predefinidos

- geral
- turma
- projeto

## Arquitetura

### Cliente

- Interface textual com menu principal e subfluxos de conversa.
- Thread dedicada para ouvir mensagens recebidas em paralelo ao input do usuario.

### Servidor

- Aceita conexoes TCP concorrentes.
- Mantem usuarios online e associa cada usuario ao seu ClientHandler.
- Mantem mapa de grupos e participantes.
- Roteia mensagens de acordo com a acao recebida.

### Modelo de Dominio

- Chat (abstrata): comportamento comum.
- SingleChat: chat de dois participantes.
- GroupChat: gerenciamento dinamico de participantes.
- Message e User: entidades principais de mensagem e usuario.

## Protocolo de Comunicacao

Pacotes serializados com a classe ChatPacket.

### Tipo

- REQUEST
- REPLY

### Acoes suportadas

- REGISTER
- BROADCAST
- LIST_USERS
- LIST_GROUPS
- JOIN_GROUP
- PRIVATE_MESSAGE
- GROUP_MESSAGE
- DISCONNECT
- SYSTEM

## Estrutura Principal

- src/main/java/br/ufc/quixada/chat/client/ChatClient.java
- src/main/java/br/ufc/quixada/chat/server/ChatServer.java
- src/main/java/br/ufc/quixada/chat/server/ClientHandler.java
- src/main/java/br/ufc/quixada/chat/core/model/ChatPacket.java
- src/main/java/br/ufc/quixada/chat/core/model/Message.java
- src/main/java/br/ufc/quixada/chat/core/model/User.java
- src/main/java/br/ufc/quixada/chat/core/model/GroupChat.java
- src/main/java/br/ufc/quixada/chat/core/model/SingleChat.java

## Como Executar

### 1) Compilar

No PowerShell, na raiz do projeto:

```powershell
$out = Join-Path $PWD "out"
if (Test-Path $out) { Remove-Item $out -Recurse -Force }
New-Item -ItemType Directory -Path $out | Out-Null
$sources = Get-ChildItem -Recurse -Filter *.java .\src\main\java | ForEach-Object { $_.FullName }
javac -d $out $sources
```

### 2) Subir o servidor

```powershell
java -cp out br.ufc.quixada.chat.server.ChatServer
```

### 3) Subir cliente(s)

Em outro terminal:

```powershell
java -cp out br.ufc.quixada.chat.client.ChatClient
```

Opcional (host e porta):

```powershell
java -cp out br.ufc.quixada.chat.client.ChatClient localhost 12345
```

## Fluxo Rapido de Uso

1. Inicie o servidor.
2. Inicie dois ou mais clientes.
3. Registre nomes diferentes.
4. Use a opcao 2 para listar conectados.
5. Use a opcao 3 para chat privado.
6. Use a opcao 4 e 5 para listar/entrar em grupos.
7. Dentro de privado ou grupo, use /voltar para retornar ao menu.
