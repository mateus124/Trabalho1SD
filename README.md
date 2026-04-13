# Trabalho 1 - Sistemas Distribuidos

Dupla: Francisco Mateus | Mateus Dodo

## Aplicacao

Sistema de chat via TCP com serializacao de objetos usando ObjectOutputStream e ObjectInputStream.

## Repositorio Core

O modulo core concentra as entidades de dominio do chat e a interface principal de operacoes.

### Estrutura criada

- src/main/java/br/ufc/quixada/chat/core/interface/ChatInterface.java
- src/main/java/br/ufc/quixada/chat/core/model/Chat.java
- src/main/java/br/ufc/quixada/chat/core/model/SingleChat.java
- src/main/java/br/ufc/quixada/chat/core/model/GroupChat.java
- src/main/java/br/ufc/quixada/chat/core/model/Message.java
- src/main/java/br/ufc/quixada/chat/core/model/User.java

## Modelo de classes

### Superclasse

- Chat

### Subclasses

- SingleChat
- GroupChat

### Agregacao

- Chat possui um conjunto de Messages.
- GroupChat possui um conjunto de Users.
- Users participam de Chats (single ou grupo).

### Interface

ChatInterface define:

- enviarMensagem(...)
- deletarMensagem(...)
- listarMensagens()
- gerenciarUsuarios(...)

## Observacoes de implementacao

- O Chat e abstrato e concentra comportamento comum para mensagens.
- SingleChat limita o chat a dois participantes.
- GroupChat gerencia participantes de forma dinamica.
- O protocolo de rede usa ChatPacket com request/reply para empacotar e desempacotar mensagens.
- O cliente em terminal oferece broadcast, listagem de conectados, chat privado e entrada em grupos predefinidos.
- Os grupos iniciais sao `geral`, `turma` e `projeto`.
