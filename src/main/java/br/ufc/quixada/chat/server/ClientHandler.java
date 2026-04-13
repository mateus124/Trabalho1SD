package br.ufc.quixada.chat.server;

import br.ufc.quixada.chat.core.model.ChatPacket;
import br.ufc.quixada.chat.core.model.Message;
import br.ufc.quixada.chat.core.model.MessageInputStream;
import br.ufc.quixada.chat.core.model.MessageOutputStream;
import br.ufc.quixada.chat.core.model.User;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ChatServer chatServer;
    private MessageInputStream input;
    private MessageOutputStream output;
    private User usuario;

    public ClientHandler(Socket socket, ChatServer chatServer) {
        this.socket = socket;
        this.chatServer = chatServer;
    }

    @Override
    public void run() {
        try {
            output = new MessageOutputStream(socket.getOutputStream());
            input = new MessageInputStream(socket.getInputStream());

            while (!socket.isClosed()) {
                ChatPacket pacote = input.lerPacote();

                if (pacote == null) {
                    break;
                }

                if (pacote.getAcao() == ChatPacket.Acao.REGISTER) {
                    registrarUsuario(pacote);
                    continue;
                }

                if (usuario == null) {
                    enviarPacote(ChatPacket.responseError(pacote.getCorrelacaoId(), "Registre-se antes de usar o chat."));
                    continue;
                }

                switch (pacote.getAcao()) {
                    case BROADCAST:
                        if (pacote.getMensagem() != null) {
                            chatServer.distribuirParaOutros(pacote.getMensagem(), this);
                        }
                        break;
                    case LIST_USERS:
                        chatServer.responderListaUsuarios(pacote.getCorrelacaoId(), this);
                        break;
                    case LIST_GROUPS:
                        chatServer.responderListaGrupos(pacote.getCorrelacaoId(), this);
                        break;
                    case JOIN_GROUP:
                        chatServer.entrarNoGrupo(usuario.getId(), pacote.getGrupoId(), pacote.getCorrelacaoId(), this);
                        break;
                    case PRIVATE_MESSAGE:
                        if (pacote.getMensagem() != null) {
                            chatServer.enviarMensagemPrivada(usuario.getId(), pacote.getDestinoId(), pacote.getMensagem());
                        }
                        break;
                    case GROUP_MESSAGE:
                        if (pacote.getMensagem() != null) {
                            chatServer.enviarMensagemGrupo(usuario.getId(), pacote.getGrupoId(), pacote.getMensagem());
                        }
                        break;
                    case DISCONNECT:
                        enviarPacote(ChatPacket.responseAck(pacote.getCorrelacaoId(), "Desconectado com sucesso."));
                        return;
                    default:
                        enviarPacote(ChatPacket.responseError(pacote.getCorrelacaoId(), "Ação não suportada."));
                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[Servidor] Falha no cliente " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
        } finally {
            fecharConexao();
            chatServer.removerCliente(this);
            System.out.println("[Servidor] Cliente desconectado: " + socket.getRemoteSocketAddress());
        }
    }

    public synchronized void enviarPacote(ChatPacket pacote) {
        try {
            if (output != null) {
                output.enviarPacote(pacote);
            }
        } catch (IOException e) {
            System.out.println("[Servidor] Erro ao enviar para " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
            fecharConexao();
            chatServer.removerCliente(this);
        }
    }

    public User getUsuario() {
        return usuario;
    }

    private void registrarUsuario(ChatPacket pacote) {
        User novoUsuario = new User(pacote.getSenderNome());
        novoUsuario.setId(pacote.getSenderId());
        usuario = novoUsuario;
        chatServer.registrarUsuario(novoUsuario, this);
        enviarPacote(ChatPacket.responseAck(pacote.getCorrelacaoId(), "Bem-vindo, " + novoUsuario.getNome() + "."));
    }

    private void fecharConexao() {
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("[Servidor] Erro ao fechar socket: " + e.getMessage());
            }
        }
    }
}
