package br.ufc.quixada.chat.core.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SingleChat extends Chat implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final int LIMITE_PARTICIPANTES = 2;

    public SingleChat(User userA, User userB) {
        super(criarParticipantes(userA, userB));
        userA.participarChat(getId());
        userB.participarChat(getId());
    }

    private static Set<User> criarParticipantes(User userA, User userB) {
        Objects.requireNonNull(userA, "userA não pode ser nulo");
        Objects.requireNonNull(userB, "userB não pode ser nulo");

        Set<User> base = new HashSet<>();
        base.add(userA);
        base.add(userB);

        if (base.size() != LIMITE_PARTICIPANTES) {
            throw new IllegalArgumentException("SingleChat exige dois usuários distintos");
        }
        return base;
    }

    @Override
    public void gerenciarUsuarios(String acao, User user) {
        Objects.requireNonNull(acao, "ação não pode ser nula");
        Objects.requireNonNull(user, "user não pode ser nulo");

        if ("remover".equalsIgnoreCase(acao)) {
            boolean removido = participantes.remove(user);
            if (removido) {
                user.sairDoChat(getId());
            }
            return;
        }

        if ("adicionar".equalsIgnoreCase(acao)) {
            if (participantes.size() >= LIMITE_PARTICIPANTES) {
                throw new IllegalStateException("SingleChat não pode ter mais de dois participantes");
            }
            participantes.add(user);
            user.participarChat(getId());
            return;
        }

        throw new IllegalArgumentException("Ação inválida. Use adicionar ou remover");
    }
}
