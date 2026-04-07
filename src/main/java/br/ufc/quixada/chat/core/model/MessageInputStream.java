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
            String id = lerStringFixa(tamanhosAtributos[0]).trim();
            String remetenteId = lerStringFixa(tamanhosAtributos[1]).trim();
            String nome = lerStringFixa(tamanhosAtributos[2]).trim(); 
            String conteudo = lerStringFixa(tamanhosAtributos[3]).replace("\0", "");

            Message msg = new Message(id, remetenteId, nome, conteudo, LocalDateTime.now());
            
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
                if (totalLido == 0) return ""; 
                throw new IOException("Fim de fluxo inesperado ao ler atributo!");
            }
            totalLido += bytesLidos;
        }
        
        return new String(buffer, StandardCharsets.UTF_8);
    }

    private void validarLayout(int[] tamanhosAtributos) {
        if (tamanhosAtributos == null || tamanhosAtributos.length < 4) {
            throw new IllegalArgumentException("Layout inválido: esperado [id, remetenteId, nome, conteudo]");
        }
    }

    @Override
    public int read() throws IOException {
        return in.read(); 
    }
}