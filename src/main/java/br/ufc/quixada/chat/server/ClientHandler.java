package br.ufc.quixada.chat.server;

import br.ufc.quixada.chat.core.model.Message;
import br.ufc.quixada.chat.core.model.MessageInputStream;
import br.ufc.quixada.chat.core.model.MessageOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer chatServer;
    private final int[] tamanhosAtributos;

    public ClientHandler(Socket socket, ChatServer chatServer, int[] tamanhosAtributos) {
        this.socket = socket;
        this.chatServer = chatServer;
        this.tamanhosAtributos = tamanhosAtributos;
    }

    @Override
    public void run() {
        try (MessageInputStream input = new MessageInputStream(socket.getInputStream())) {
            while (!socket.isClosed()) {
                List<Message> recebidas = input.lerDados(tamanhosAtributos);

                if (recebidas.isEmpty()) {
                    break;
                }

                for (Message message : recebidas) {
                    chatServer.distribuirParaOutros(message, this);
                }
            }
        } catch (IOException e) {
            System.out.println("[Servidor] Falha no cliente " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
        } finally {
            fecharConexao();
            chatServer.removerCliente(this);
            System.out.println("[Servidor] Cliente desconectado: " + socket.getRemoteSocketAddress());
        }
    }

    public synchronized void enviarMensagem(Message message) {
        try {
            Message[] mensagens = {message};
            MessageOutputStream output = new MessageOutputStream(mensagens, 1, tamanhosAtributos, socket.getOutputStream());
            output.enviarDados();
        } catch (IOException e) {
            System.out.println("[Servidor] Erro ao enviar para " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
            fecharConexao();
            chatServer.removerCliente(this);
        }
    }

    private void fecharConexao() {
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("[Servidor] Erro ao fechar socket: " + e.getMessage());
            }
        }
    }
}
