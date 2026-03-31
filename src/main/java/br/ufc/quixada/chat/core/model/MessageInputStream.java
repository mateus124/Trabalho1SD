package br.ufc.quixada.chat.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessageInputStream extends InputStream {
    private final InputStream in;

    public MessageInputStream(InputStream in) {
        this.in = in;
    }

    public List<Message> lerDados(int[] tamanhosAtributos) throws IOException {
        List<Message> mensagensLidas = new ArrayList<>();

        int quantidade = in.read();
        if (quantidade == -1) return mensagensLidas; 

        for (int i = 0; i < quantidade; i++) {
            // Lendo Atributo 1: ID
            String id = lerStringFixa(tamanhosAtributos[0]);
            
            String remetenteId = lerStringFixa(tamanhosAtributos[1]);
            
            String conteudo = lerStringFixa(tamanhosAtributos[2]);

            Message msg = new Message(remetenteId, "Nome_Recuperado", conteudo);
            
            mensagensLidas.add(msg);
        }

        return mensagensLidas;
    }

    private String lerStringFixa(int tamanho) throws IOException {
        byte[] buffer = new byte[tamanho];
        int bytesLidos = in.read(buffer);
        if (bytesLidos == -1) throw new IOException("Fim de fluxo inesperado!");
        
        return new String(buffer, StandardCharsets.UTF_8).trim();
    }

    @Override
    public int read() throws IOException {
        return in.read(); 
    }
}