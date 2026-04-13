package br.ufc.quixada.chat.server;

import br.ufc.quixada.chat.core.model.ChatPacket;
import br.ufc.quixada.chat.core.model.GroupChat;
import br.ufc.quixada.chat.core.model.Message;
import br.ufc.quixada.chat.core.model.User;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChatServer {
    private static final int PORTA_PADRAO = 12345;
    private final int porta;
    private final List<ClientHandler> clientes;
    private final Map<String, ClientHandler> clientesPorUsuarioId;
    private final Map<String, User> usuariosPorId;
    private final Map<String, GroupChat> grupos;

    public ChatServer() {
        this(PORTA_PADRAO);
    }

    public ChatServer(int porta) {
        this.porta = porta;
        this.clientes = new CopyOnWriteArrayList<>();
        this.clientesPorUsuarioId = new ConcurrentHashMap<>();
        this.usuariosPorId = new ConcurrentHashMap<>();
        this.grupos = new ConcurrentHashMap<>();
        criarGruposPadrao();
    }

    private void criarGruposPadrao() {
        grupos.put("geral", new GroupChat(Collections.emptySet()));
        grupos.put("turma", new GroupChat(Collections.emptySet()));
        grupos.put("projeto", new GroupChat(Collections.emptySet()));
    }

    public void iniciar() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            System.out.println("[Servidor] ChatServer iniciado na porta " + porta);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("[Servidor] Cliente conectado: " + socket.getRemoteSocketAddress());

                ClientHandler clientHandler = new ClientHandler(socket, this);
                clientes.add(clientHandler);

                Thread clientThread = new Thread(clientHandler);
                clientThread.start();
            }
        }
    }

    public void registrarUsuario(User usuario, ClientHandler handler) {
        usuariosPorId.put(usuario.getId(), usuario);
        clientesPorUsuarioId.put(usuario.getId(), handler);
    }

    public void distribuirParaOutros(Message message, ClientHandler remetente) {
        for (ClientHandler cliente : clientes) {
            cliente.enviarPacote(ChatPacket.eventBroadcastMessage(message));
        }
    }

    public void responderAoRemetente(String correlacaoId, ClientHandler remetente, String resposta) {
        remetente.enviarPacote(ChatPacket.responseAck(correlacaoId, resposta));
    }

    public void responderListaUsuarios(String correlacaoId, ClientHandler remetente) {
        List<User> usuariosOrdenados = new ArrayList<>(usuariosPorId.values());
        usuariosOrdenados.sort(Comparator.comparing(User::getNome, String.CASE_INSENSITIVE_ORDER));

        List<String> usuariosFormatados = new ArrayList<>();
        for (User usuario : usuariosOrdenados) {
            usuariosFormatados.add(usuario.getId() + "|" + usuario.getNome());
        }

        remetente.enviarPacote(ChatPacket.responseListUsers(correlacaoId, usuariosFormatados));
    }

    public void responderListaGrupos(String correlacaoId, ClientHandler remetente) {
        List<String> nomesOrdenados = new ArrayList<>(grupos.keySet());
        Collections.sort(nomesOrdenados, String.CASE_INSENSITIVE_ORDER);

        List<String> gruposFormatados = new ArrayList<>();
        for (String nomeGrupo : nomesOrdenados) {
            GroupChat grupo = grupos.get(nomeGrupo);
            gruposFormatados.add(nomeGrupo + "|" + grupo.getParticipantes().size());
        }

        remetente.enviarPacote(ChatPacket.responseListGroups(correlacaoId, gruposFormatados));
    }

    public void entrarNoGrupo(String usuarioId, String grupoId, String correlacaoId, ClientHandler remetente) {
        User usuario = usuariosPorId.get(usuarioId);
        GroupChat grupo = grupos.get(grupoId);

        if (usuario == null) {
            remetente.enviarPacote(ChatPacket.responseError(correlacaoId, "Usuário não registrado."));
            return;
        }

        if (grupo == null) {
            remetente.enviarPacote(ChatPacket.responseError(correlacaoId, "Grupo não encontrado."));
            return;
        }

        grupo.gerenciarUsuarios("adicionar", usuario);
        remetente.enviarPacote(ChatPacket.responseAck(correlacaoId, "Você entrou no grupo " + grupoId + "."));
    }

    public void enviarMensagemPrivada(String senderId, String destinoId, Message message) {
        ClientHandler remetente = clientesPorUsuarioId.get(senderId);
        ClientHandler destino = clientesPorUsuarioId.get(destinoId);

        if (remetente == null) {
            return;
        }

        if (destino == null) {
            remetente.enviarPacote(ChatPacket.responseError(message.getId(), "Usuário offline ou não encontrado."));
            return;
        }

        destino.enviarPacote(ChatPacket.eventPrivateMessage(message, destinoId));
        if (remetente != destino) {
            remetente.enviarPacote(ChatPacket.eventPrivateMessage(message, destinoId));
        }
    }

    public void enviarMensagemGrupo(String senderId, String grupoId, Message message) {
        ClientHandler remetente = clientesPorUsuarioId.get(senderId);
        GroupChat grupo = grupos.get(grupoId);

        if (remetente == null) {
            return;
        }

        if (grupo == null) {
            remetente.enviarPacote(ChatPacket.responseError(message.getId(), "Grupo não encontrado."));
            return;
        }

        User usuario = usuariosPorId.get(senderId);
        if (usuario == null || !grupo.getParticipantes().contains(usuario)) {
            remetente.enviarPacote(ChatPacket.responseError(message.getId(), "Você precisa entrar no grupo antes de enviar mensagens."));
            return;
        }

        for (User participante : grupo.getParticipantes()) {
            ClientHandler cliente = clientesPorUsuarioId.get(participante.getId());
            if (cliente != null) {
                cliente.enviarPacote(ChatPacket.eventGroupMessage(message, grupoId));
            }
        }
    }

    public void removerCliente(ClientHandler clientHandler) {
        clientes.remove(clientHandler);

        User usuario = clientHandler.getUsuario();
        if (usuario == null) {
            return;
        }

        clientesPorUsuarioId.remove(usuario.getId());
        usuariosPorId.remove(usuario.getId());

        for (GroupChat grupo : grupos.values()) {
            grupo.gerenciarUsuarios("remover", usuario);
        }
    }

    public static void main(String[] args) {
        int porta = PORTA_PADRAO;

        if (args.length > 0) {
            porta = Integer.parseInt(args[0]);
        }

        ChatServer server = new ChatServer(porta);
        try {
            server.iniciar();
        } catch (IOException e) {
            System.out.println("[Servidor] Erro ao iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
