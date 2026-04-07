package br.ufc.quixada.chat.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class TesteStreams {
    public static void main(String[] args) throws Exception {
        Message m1 = new Message("user1", "Chico", "Olá, tudo bem?");
        Message m2 = new Message("user2", "Dodó", "Tudo certo!");
        Message[] mensagens = {m1, m2};

        int[] tamanhos = {36, 36, 64};

        System.out.println("--- TESTE 1: Gravando e lendo em arquivo ---");
        
        try (FileOutputStream fileOut = new FileOutputStream("dados_chat.bin")) {
            MessageOutputStream customOut = new MessageOutputStream(mensagens, 2, tamanhos, fileOut);
            customOut.enviarDados();    
            System.out.println("Dados salvos em dados_chat.bin!");
        }

        try (FileInputStream fileIn = new FileInputStream("dados_chat.bin")) {
            MessageInputStream customIn = new MessageInputStream(fileIn);
            List<Message> lidas = customIn.lerDados(tamanhos);

            validarMensagens(mensagens, lidas, "arquivo");
            System.out.println("OK: mensagens lidas do arquivo com sucesso.");
        }

        System.out.println("\n--- TESTE 2: System.out / System.in ---");
        ByteArrayOutputStream bufferBytes = new ByteArrayOutputStream();
        MessageOutputStream customOutMemoria = new MessageOutputStream(mensagens, 2, tamanhos, bufferBytes);
        customOutMemoria.enviarDados();

        // Simula escrita via System.out para visualização (bytes binários podem ficar ilegíveis).
        MessageOutputStream customOutConsole = new MessageOutputStream(mensagens, 2, tamanhos, System.out);
        customOutConsole.enviarDados();
        System.out.println("\nBytes também enviados para System.out.");

        ByteArrayInputStream entradaSimulada = new ByteArrayInputStream(bufferBytes.toByteArray());
        InputStream entradaOriginal = System.in;

        try {
            System.setIn(entradaSimulada);
            MessageInputStream inputPadrao = new MessageInputStream(System.in);
            List<Message> lidasIn = inputPadrao.lerDados(tamanhos);
            validarMensagens(mensagens, lidasIn, "System.in");
            System.out.println("OK: mensagens lidas com sucesso via System.in.");
        } finally {
            System.setIn(entradaOriginal);
        }
    }

    private static void validarMensagens(Message[] esperadas, List<Message> lidas, String origem) {
        if (lidas.size() != esperadas.length) {
            throw new IllegalStateException("Falha no teste de " + origem + ": quantidade de mensagens diferente.");
        }

        for (int i = 0; i < esperadas.length; i++) {
            Message esperada = esperadas[i];
            Message atual = lidas.get(i);

            if (!esperada.getId().equals(atual.getId())) {
                throw new IllegalStateException("Falha no teste de " + origem + ": id diferente no índice " + i);
            }
            if (!esperada.getRemetenteId().equals(atual.getRemetenteId())) {
                throw new IllegalStateException("Falha no teste de " + origem + ": remetenteId diferente no índice " + i);
            }
            if (!esperada.getConteudo().equals(atual.getConteudo())) {
                throw new IllegalStateException("Falha no teste de " + origem + ": conteúdo diferente no índice " + i);
            }
        }
    }
}
