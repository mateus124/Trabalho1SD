package br.ufc.quixada.chat.server;

import br.ufc.quixada.chat.core.model.Message;
import br.ufc.quixada.chat.core.model.MessageInputStream;
import br.ufc.quixada.chat.core.model.MessageOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer servidor;
    private final int[] tamanhosAtributos;

    public ClientHandler(Socket socket, ChatServer servidor, int[] tamanhosAtributos) {
        this.socket = socket;
        this.servidor = servidor;
        this.tamanhosAtributos = tamanhosAtributos; // Garanta que isso tem 4 posições
    }

    @Override
    public void run() {
        try {
            MessageInputStream input = new MessageInputStream(socket.getInputStream());
            
            while (!socket.isClosed()) {
                // Aqui é onde o erro acontecia se o array tivesse apenas 3 posições
                List<Message> mensagens = input.lerDados(tamanhosAtributos);
                
                for (Message msg : mensagens) {
                    servidor.distribuirParaOutros(msg, this);
                }
            }
        } catch (IOException e) {
            System.out.println("[Servidor] Cliente desconectado: " + socket.getRemoteSocketAddress());
        } finally {
            servidor.removerCliente(this);
            fecharSocket();
        }
    }

    public void enviarMensagem(Message message) {
        try {
            Message[] msgs = {message};
            // Aqui também passamos o array de 4 posições
            MessageOutputStream output = new MessageOutputStream(msgs, 1, tamanhosAtributos, socket.getOutputStream());
            output.enviarDados();
        } catch (IOException e) {
            fecharSocket();
        }
    }

    private void fecharSocket() {
        try {
            if (!socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}