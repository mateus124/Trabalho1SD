package br.ufc.quixada.chat.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class User {
    private final String id;
    private final String nome;
    private final Set<String> chatIds;

    public User(String nome) {
        this.id = UUID.randomUUID().toString();
        this.nome = Objects.requireNonNull(nome, "nome não pode ser nulo");
        this.chatIds = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Set<String> getChatIds() {
        return Collections.unmodifiableSet(chatIds);
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
