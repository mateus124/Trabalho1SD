package br.ufc.quixada.chat.server;

import br.ufc.quixada.chat.core.model.Message;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {
    private static final int PORTA_PADRAO = 12345;
    private static final int[] TAMANHOS_ATRIBUTOS = {36, 36, 36, 5000};
    private final int porta;
    private final List<ClientHandler> clientes;

    public ChatServer() {
        this(PORTA_PADRAO);
    }

    public ChatServer(int porta) {
        this.porta = porta;
        this.clientes = new CopyOnWriteArrayList<>();
    }

    public void iniciar() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("[Servidor] ChatServer iniciado na porta " + porta);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[Servidor] Cliente conectado: " + socket.getRemoteSocketAddress());

                ClientHandler clientHandler = new ClientHandler(socket, this, TAMANHOS_ATRIBUTOS);
                clientes.add(clientHandler);

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        }
    }

    public void distribuirParaOutros(Message message, ClientHandler remetente) {
        for (ClientHandler cliente : clientes) {
            if (cliente != remetente) {
                cliente.enviarMensagem(message);
            }
        }
    }

    public void removerCliente(ClientHandler clientHandler) {
        clientes.remove(clientHandler);
    }

    public static void main(String[] args) {
        int porta = PORTA_PADRAO;

        if (args.length > 0) {
            porta = Integer.parseInt(args[0]);
        }

        ChatServer server = new ChatServer(porta);
        try {
            server.iniciar();
        } catch (IOException e) {
            System.out.println("[Servidor] Erro ao iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
