package br.ufc.quixada.chat.core.model;

import java.io.*;
import java.net.*;

public class TesteRedeTCP {
    public static void main(String[] args) throws Exception {
        int porta = 12345;

        // Thread para o SERVIDOR
        Thread servidor = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(porta)) {
                System.out.println("[Servidor] Aguardando conexão...");
                try (Socket socket = serverSocket.accept();
                     MessageOutputStream mos = new MessageOutputStream(socket.getOutputStream());
                     MessageInputStream mis = new MessageInputStream(socket.getInputStream())) {

                    ChatPacket request = mis.lerPacote();
                    if (!request.isRequest()) {
                        throw new IllegalStateException("[Servidor] pacote recebido não é request.");
                    }

                    Message recebida = request.getMensagem();
                    if (!"user-rede".equals(recebida.getRemetenteId())) {
                        throw new IllegalStateException("[Servidor] remetenteId inválido.");
                    }
                    if (!"Olá via TCP!".equals(recebida.getConteudo())) {
                        throw new IllegalStateException("[Servidor] conteúdo inválido.");
                    }

                    mos.enviarPacote(ChatPacket.reply(recebida.getId(), "Mensagem recebida pelo servidor"));
                    System.out.println("[Servidor] OK: request recebido e reply enviado.");
                }
            } catch (IOException | ClassNotFoundException e) { e.printStackTrace(); }
        });

        servidor.start();
        Thread.sleep(1000); 

        // Lógica do CLIENTE
        System.out.println("[Cliente] Conectando ao servidor...");
        try (Socket socket = new Socket("localhost", porta);
             MessageOutputStream mos = new MessageOutputStream(socket.getOutputStream());
             MessageInputStream mis = new MessageInputStream(socket.getInputStream())) {
            Message m = new Message("user-rede", "Mateus", "Olá via TCP!");

            mos.enviarPacote(ChatPacket.request(m));
            System.out.println("[Cliente] Request enviado!");

            ChatPacket reply = mis.lerPacote();
            if (!reply.isReply()) {
                throw new IllegalStateException("[Cliente] pacote de resposta inválido.");
            }
            if (!m.getId().equals(reply.getCorrelacaoId())) {
                throw new IllegalStateException("[Cliente] correlação do reply inválida.");
            }
            System.out.println("[Cliente] OK: reply recebido e validado.");
        }
        
        servidor.join(2000);
    }
}