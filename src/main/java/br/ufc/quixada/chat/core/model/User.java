package br.ufc.quixada.chat.core.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String nome;
    private Set<String> chatIds;

    public User(String nome) {
        this.id = UUID.randomUUID().toString();
        this.nome = Objects.requireNonNull(nome, "nome não pode ser nulo");
        this.chatIds = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id não pode ser nulo");
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = Objects.requireNonNull(nome, "nome não pode ser nulo");
    }

    public Set<String> getChatIds() {
        return Collections.unmodifiableSet(chatIds);
    }

    public void setChatIds(Set<String> chatIds) {
        this.chatIds = new HashSet<>(Objects.requireNonNull(chatIds, "chatIds não pode ser nulo"));
    }

    public void participarChat(String chatId) {
        chatIds.add(chatId);
    }

    public void sairDoChat(String chatId) {
        chatIds.remove(chatId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
