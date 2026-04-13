package br.ufc.quixada.chat.core.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class TesteStreams {
    public static void main(String[] args) throws Exception {
        Message m1 = new Message("user1", "Chico", "Olá, tudo bem?");
        ChatPacket request = ChatPacket.request(m1);
        ChatPacket reply = ChatPacket.reply(m1.getId(), "Mensagem recebida pelo servidor");

        System.out.println("--- TESTE 1: Request/Reply em memória ---");
        ByteArrayOutputStream bufferBytes = new ByteArrayOutputStream();
        try (MessageOutputStream saida = new MessageOutputStream(bufferBytes)) {
            saida.enviarPacote(request);
            saida.enviarPacote(reply);
            System.out.println("Pacotes serializados em memória.");
        }

        try (ByteArrayInputStream entradaSimulada = new ByteArrayInputStream(bufferBytes.toByteArray());
             MessageInputStream entrada = new MessageInputStream(entradaSimulada)) {
            ChatPacket requestLida = entrada.lerPacote();
            ChatPacket replyLida = entrada.lerPacote();

            validarRequest(request, requestLida);
            validarReply(reply, replyLida);
            System.out.println("OK: request/reply serializados e desserializados com sucesso.");
        }
    }

    private static void validarRequest(ChatPacket esperada, ChatPacket atual) {
        if (!atual.isRequest()) {
            throw new IllegalStateException("Falha no teste de request: tipo incorreto.");
        }
        if (!esperada.getMensagem().equals(atual.getMensagem())) {
            throw new IllegalStateException("Falha no teste de request: mensagem diferente.");
        }
        if (!esperada.getCorrelacaoId().equals(atual.getCorrelacaoId())) {
            throw new IllegalStateException("Falha no teste de request: correlação diferente.");
        }
    }

    private static void validarReply(ChatPacket esperada, ChatPacket atual) {
        if (!atual.isReply()) {
            throw new IllegalStateException("Falha no teste de reply: tipo incorreto.");
        }
        if (!esperada.getCorrelacaoId().equals(atual.getCorrelacaoId())) {
            throw new IllegalStateException("Falha no teste de reply: correlação diferente.");
        }
        if (!esperada.getResposta().equals(atual.getResposta())) {
            throw new IllegalStateException("Falha no teste de reply: resposta diferente.");
        }
    }
}
