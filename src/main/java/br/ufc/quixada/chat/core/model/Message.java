package br.ufc.quixada.chat.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String remetenteId;
    private String remetenteNome;
    private String conteudo;
    private LocalDateTime dataHora;

    // Construtor usado pelo CLIENTE ao criar uma nova mensagem para enviar
    public Message(String remetenteId, String remetenteNome, String conteudo) {
        this(UUID.randomUUID().toString(), remetenteId, remetenteNome, conteudo, LocalDateTime.now());
    }

    // Construtor usado pelo INPUTSTREAM ao reconstruir a mensagem que veio da rede
    public Message(String id, String remetenteId, String remetenteNome, String conteudo, LocalDateTime dataHora) {
        this.id = Objects.requireNonNull(id, "id não pode ser nulo");
        this.remetenteId = Objects.requireNonNull(remetenteId, "remetenteId não pode ser nulo");
        this.remetenteNome = Objects.requireNonNull(remetenteNome, "remetenteNome não pode ser nulo");
        this.conteudo = Objects.requireNonNull(conteudo, "conteúdo não pode ser nulo");
        this.dataHora = Objects.requireNonNull(dataHora, "dataHora não pode ser nula");
    }

    // Getters e Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRemetenteId() { return remetenteId; }
    public void setRemetenteId(String remetenteId) { this.remetenteId = remetenteId; }

    public String getRemetenteNome() { return remetenteNome; }
    public void setRemetenteNome(String remetenteNome) { this.remetenteNome = remetenteNome; }

    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }
}