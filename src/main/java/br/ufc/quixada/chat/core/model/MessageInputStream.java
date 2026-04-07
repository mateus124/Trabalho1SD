package br.ufc.quixada.chat.core.model;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MessageInputStream extends InputStream {
    private final InputStream in;

    public MessageInputStream(InputStream in) {
        this.in = in;
    }

    public List<Message> lerDados(int[] tamanhosAtributos) throws IOException {
        validarLayout(tamanhosAtributos);
        List<Message> mensagensLidas = new ArrayList<>();

        int quantidade = in.read();
        if (quantidade == -1) return mensagensLidas; 

        for (int i = 0; i < quantidade; i++) {
            String id = lerStringFixa(tamanhosAtributos[0]);
            String remetenteId = lerStringFixa(tamanhosAtributos[1]);
            String conteudo = lerStringFixa(tamanhosAtributos[2]);

            Message msg = new Message(id, remetenteId, "Nome_Recuperado", conteudo, LocalDateTime.now());
            
            mensagensLidas.add(msg);
        }

        return mensagensLidas;
    }

    private String lerStringFixa(int tamanho) throws IOException {
        byte[] buffer = new byte[tamanho];
        int totalLido = 0;

        while (totalLido < tamanho) {
            int bytesLidos = in.read(buffer, totalLido, tamanho - totalLido);
            if (bytesLidos == -1) {
                throw new IOException("Fim de fluxo inesperado!");
            }
            totalLido += bytesLidos;
        }
        
        return new String(buffer, StandardCharsets.UTF_8).trim();
    }

    private void validarLayout(int[] tamanhosAtributos) {
        if (tamanhosAtributos == null || tamanhosAtributos.length < 3) {
            throw new IllegalArgumentException("Layout inválido: esperado [id, remetenteId, conteudo]");
        }
    }

    @Override
    public int read() throws IOException {
        return in.read(); 
    }
}