package br.ufc.quixada.chat.core.model;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MessageOutputStream extends OutputStream {
    private final Message[] mensagens;
    private final int quantidade;
    private final int[] tamanhosAtributos;
    private final OutputStream out;

    public MessageOutputStream(Message[] mensagens, int quantidade, int[] tamanhosAtributos, OutputStream out) {
        this.mensagens = mensagens;
        this.quantidade = quantidade;
        this.tamanhosAtributos = tamanhosAtributos;
        this.out = out;
    }

    public void enviarDados() throws IOException {
        out.write(quantidade);

        for (int i = 0; i < quantidade; i++) {
            Message msg = mensagens[i];

            escreverStringFixa(msg.getId(), tamanhosAtributos[0]);

            escreverStringFixa(msg.getRemetenteId(), tamanhosAtributos[1]);

            escreverStringFixa(msg.getConteudo(), tamanhosAtributos[2]);
        }
        out.flush(); 
    }

    private void escreverStringFixa(String valor, int tamanho) throws IOException {
        byte[] bytes = valor.getBytes(StandardCharsets.UTF_8);
        byte[] buffer = new byte[tamanho];
        
        System.arraycopy(bytes, 0, buffer, 0, Math.min(bytes.length, tamanho));
        
        out.write(buffer);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b); 
    }
}
