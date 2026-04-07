package br.ufc.quixada.chat.core.model;

import java.io.*;
import java.net.*;
import java.util.List;

public class TesteRedeTCP {
    public static void main(String[] args) throws Exception {
        int porta = 12345;
        int[] tamanhos = {36, 36, 64};

        // Thread para o SERVIDOR
        Thread servidor = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(porta)) {
                System.out.println("[Servidor] Aguardando conexão...");
                try (Socket socket = serverSocket.accept();
                     MessageInputStream mis = new MessageInputStream(socket.getInputStream())) {
                    
                    List<Message> recebidas = mis.lerDados(tamanhos);
                    if (recebidas.size() != 1) {
                        throw new IllegalStateException("[Servidor] Quantidade de mensagens inválida.");
                    }

                    Message recebida = recebidas.get(0);
                    if (!"user-rede".equals(recebida.getRemetenteId())) {
                        throw new IllegalStateException("[Servidor] remetenteId inválido.");
                    }
                    if (!"Olá via TCP!".equals(recebida.getConteudo())) {
                        throw new IllegalStateException("[Servidor] conteúdo inválido.");
                    }

                    System.out.println("[Servidor] OK: mensagem recebida via rede e validada.");
                }
            } catch (IOException e) { e.printStackTrace(); }
        });

        servidor.start();
        Thread.sleep(1000); 

        // Lógica do CLIENTE
        System.out.println("[Cliente] Conectando ao servidor...");
        try (Socket socket = new Socket("localhost", porta)) {
            Message m = new Message("user-rede", "Mateus", "Olá via TCP!");
            Message[] msgs = { m };
            
            MessageOutputStream mos = new MessageOutputStream(msgs, 1, tamanhos, socket.getOutputStream());
            mos.enviarDados();
            System.out.println("[Cliente] Dados enviados!");
        }
        
        servidor.join(2000);
    }
}