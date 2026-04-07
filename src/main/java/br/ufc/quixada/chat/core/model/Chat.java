package br.ufc.quixada.chat.core.model;

import br.ufc.quixada.chat.core.interfaces.ChatInterface;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public abstract class Chat implements ChatInterface, Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    protected Set<User> participantes;
    protected List<Message> messages;

    protected Chat(Set<User> participantes) {
        this.id = UUID.randomUUID().toString();
        this.participantes = Objects.requireNonNull(participantes, "participantes não pode ser nulo");
        this.messages = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Objects.requireNonNull(id, "id não pode ser nulo");
    }

    public Set<User> getParticipantes() {
        return Collections.unmodifiableSet(participantes);
    }

    public void setParticipantes(Set<User> participantes) {
        this.participantes = Objects.requireNonNull(participantes, "participantes não pode ser nulo");
    }

    public List<Message> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void setMessages(List<Message> messages) {
        this.messages = Objects.requireNonNull(messages, "messages não pode ser nulo");
    }

    @Override
    public void enviarMensagem(User remetente, String conteudo) {
        Objects.requireNonNull(remetente, "remetente não pode ser nulo");
        Objects.requireNonNull(conteudo, "conteúdo não pode ser nulo");

        if (!participantes.contains(remetente)) {
            throw new IllegalArgumentException("Usuário não participa deste chat");
        }

        Message mensagem = new Message(remetente.getId(), remetente.getNome(), conteudo);
        messages.add(mensagem);
    }

    @Override
    public boolean deletarMensagem(String messageId) {
        return messages.removeIf(msg -> msg.getId().equals(messageId));
    }

    @Override
    public List<Message> listarMensagens() {
        return Collections.unmodifiableList(messages);
    }
}
