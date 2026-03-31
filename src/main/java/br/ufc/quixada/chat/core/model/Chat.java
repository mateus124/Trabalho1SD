package br.ufc.quixada.chat.core.model;

import br.ufc.quixada.chat.core.interface.ChatInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public abstract class Chat implements ChatInterface {
    private final String id;
    protected final Set<User> participantes;
    protected final List<Message> messages;

    protected Chat(Set<User> participantes) {
        this.id = UUID.randomUUID().toString();
        this.participantes = Objects.requireNonNull(participantes, "participantes não pode ser nulo");
        this.messages = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public Set<User> getParticipantes() {
        return Collections.unmodifiableSet(participantes);
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
