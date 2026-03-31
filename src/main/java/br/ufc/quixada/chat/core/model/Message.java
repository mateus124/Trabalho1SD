package br.ufc.quixada.chat.core.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Message {
    private final String id;
    private final String remetenteId;
    private final String remetenteNome;
    private final String conteudo;
    private final LocalDateTime dataHora;

    public Message(String remetenteId, String remetenteNome, String conteudo) {
        this.id = UUID.randomUUID().toString();
        this.remetenteId = Objects.requireNonNull(remetenteId, "remetenteId não pode ser nulo");
        this.remetenteNome = Objects.requireNonNull(remetenteNome, "remetenteNome não pode ser nulo");
        this.conteudo = Objects.requireNonNull(conteudo, "conteúdo não pode ser nulo");
        this.dataHora = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getRemetenteId() {
        return remetenteId;
    }

    public String getRemetenteNome() {
        return remetenteNome;
    }

    public String getConteudo() {
        return conteudo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
