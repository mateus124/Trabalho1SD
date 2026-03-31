package br.ufc.quixada.chat.core.interfaces;

import br.ufc.quixada.chat.core.model.Message;
import br.ufc.quixada.chat.core.model.User;
import java.util.List;

public interface ChatInterface {
    void enviarMensagem(User remetente, String conteudo);

    boolean deletarMensagem(String messageId);

    List<Message> listarMensagens();

    void gerenciarUsuarios(String acao, User user);
}
