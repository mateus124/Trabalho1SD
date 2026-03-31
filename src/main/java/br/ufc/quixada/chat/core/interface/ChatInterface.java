package br.ufc.quixada.chat.core.interface;

import br.ufc.quixada.chat.core.model.Message;
import br.ufc.quixada.chat.core.model.User;
import java.util.List;

public interface ChatInterface {
    void enviarMensagem(User remetente, String conteudo);

    boolean deletarMensagem(String messageId);

    List<Message> listarMensagens();

    void gerenciarUsuarios(String acao, User user);
}
