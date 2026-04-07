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
    // Protocolo: ID(36), RemetenteID(36), Nome(36), Conteúdo(5000)
    private static final int[] TAMANHOS_ATRIBUTOS = {36, 36, 36, 5000}; 

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== BEM-VINDO AO M2TALK ===");
        System.out.print("Digite seu nome: ");
        String nome = scanner.nextLine();
        User usuarioAtivo = new User(nome);

        try (Socket socket = new Socket(HOST, PORTA)) {
            System.out.println("\nConectado! (Digite 'sair' para encerrar)\n");

            // Thread para ouvir mensagens do servidor
            Thread listenerThread = new Thread(() -> {
                try {
                    MessageInputStream input = new MessageInputStream(socket.getInputStream());
                    while (!socket.isClosed()) {
                        List<Message> recebidas = input.lerDados(TAMANHOS_ATRIBUTOS);
                        if (recebidas.isEmpty()) break;
                        
                        for (Message msg : recebidas) {
                            // Limpa a linha onde o usuário está digitando
                            System.out.print("\r" + " ".repeat(80) + "\r");
                            
                            // Exibe a mensagem removendo bytes nulos para não quebrar ASCII Art
                            String exibeNome = msg.getRemetenteNome().replace("\0", "").trim();
                            String exibeConteudo = msg.getConteudo().replace("\0", "");
                            
                            System.out.println("[" + exibeNome + "]: " + exibeConteudo);
                            
                            // Reexibe o prompt
                            System.out.print("Você: "); 
                        }
                    }
                } catch (IOException e) {
                    System.out.println("\nConexão com o servidor perdida.");
                }
            });
            listenerThread.setDaemon(true);
            listenerThread.start();

            // Loop principal de envio
            while (true) {
                System.out.print("Você: ");
                String texto = scanner.nextLine();
                
                if ("sair".equalsIgnoreCase(texto)) break;
                if (texto.trim().isEmpty()) continue;

                Message msg = new Message(usuarioAtivo.getId(), usuarioAtivo.getNome(), texto);
                Message[] arrayMsg = {msg};
                
                // Envia usando a classe de saída do protocolo
                MessageOutputStream output = new MessageOutputStream(arrayMsg, 1, TAMANHOS_ATRIBUTOS, socket.getOutputStream());
                output.enviarDados();
            }

        } catch (IOException e) {
            System.out.println("Erro: Não foi possível conectar ao servidor.");
        } finally {
            scanner.close();
        }
    }
}