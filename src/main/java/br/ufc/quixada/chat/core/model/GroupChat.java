package br.ufc.quixada.chat.core.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class GroupChat extends Chat implements Serializable {
    private static final long serialVersionUID = 1L;

    public GroupChat(Set<User> usuariosIniciais) {
        super(new HashSet<>(Objects.requireNonNull(usuariosIniciais, "usuariosIniciais não pode ser nulo")));

        for (User user : participantes) {
            user.participarChat(getId());
        }
    }

    @Override
    public void gerenciarUsuarios(String acao, User user) {
        Objects.requireNonNull(acao, "ação não pode ser nula");
        Objects.requireNonNull(user, "user não pode ser nulo");

        if ("adicionar".equalsIgnoreCase(acao)) {
            boolean adicionado = participantes.add(user);
            if (adicionado) {
                user.participarChat(getId());
            }
            return;
        }

        if ("remover".equalsIgnoreCase(acao)) {
            boolean removido = participantes.remove(user);
            if (removido) {
                user.sairDoChat(getId());
            }
            return;
        }

        throw new IllegalArgumentException("Acão invalida. Use adicionar ou remover");
    }
}
