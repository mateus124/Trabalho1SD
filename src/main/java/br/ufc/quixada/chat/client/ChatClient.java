package br.ufc.quixada.chat.client;

import br.ufc.quixada.chat.core.model.Message;
import br.ufc.quixada.chat.core.model.MessageInputStream;
import br.ufc.quixada.chat.core.model.MessageOutputStream;
import br.ufc.quixada.chat.core.model.User;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

public class ChatClient {
    private static final String HOST = "localhost";
    private static final int PORTA = 12345;
    private static final int[] TAMANHOS_ATRIBUTOS = {36, 36, 64}; 

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== BEM-VINDO AO CHAT ===");
        System.out.print("Digite seu nome para entrar: ");
        String nome = scanner.nextLine();
        
        User usuarioAtivo = new User(nome);

        try (Socket socket = new Socket(HOST, PORTA)) {
            System.out.println("Conectado ao servidor! Digite suas mensagens (ou 'sair' para encerrar).");

            Thread listenerThread = new Thread(() -> {
                try {
                    MessageInputStream input = new MessageInputStream(socket.getInputStream());
                    while (!socket.isClosed()) {
                        List<Message> recebidas = input.lerDados(TAMANHOS_ATRIBUTOS);
                        
                        if (recebidas.isEmpty()) break;
                        
                        for (Message msg : recebidas) {
                            System.out.println("\n[" + msg.getRemetenteNome() + "]: " + msg.getConteudo());
                            System.out.print("Você: "); 
                        }
                    }
                } catch (IOException e) {
                    System.out.println("\n[Aviso] Conexão com o servidor encerrada.");
                }
            });
            listenerThread.start();

            while (true) {
                System.out.print("Você: ");
                String texto = scanner.nextLine();
                
                if ("sair".equalsIgnoreCase(texto)) {
                    break;
                }

                Message msg = new Message(usuarioAtivo.getId(), usuarioAtivo.getNome(), texto);
                Message[] arrayMsg = {msg};
                
                MessageOutputStream output = new MessageOutputStream(arrayMsg, 1, TAMANHOS_ATRIBUTOS, socket.getOutputStream());
                output.enviarDados();
            }

        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: Verifique se o ChatServer está rodando.");
        } finally {
            scanner.close();
            System.out.println("Chat encerrado.");
        }
    }
}