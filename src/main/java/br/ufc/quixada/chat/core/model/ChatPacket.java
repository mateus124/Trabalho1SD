package br.ufc.quixada.chat.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ChatPacket implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Tipo {
        REQUEST,
        REPLY
    }

    public enum Acao {
        REGISTER,
        BROADCAST,
        LIST_USERS,
        LIST_GROUPS,
        JOIN_GROUP,
        PRIVATE_MESSAGE,
        GROUP_MESSAGE,
        DISCONNECT,
        SYSTEM
    }

    private final Tipo tipo;
    private final Acao acao;
    private final String senderId;
    private final String senderNome;
    private final String destinoId;
    private final String grupoId;
    private final Message mensagem;
    private final List<String> itens;
    private final String correlacaoId;
    private final String resposta;

    private ChatPacket(Tipo tipo, Acao acao, String senderId, String senderNome, String destinoId, String grupoId, Message mensagem, List<String> itens, String correlacaoId, String resposta) {
        this.tipo = Objects.requireNonNull(tipo, "tipo não pode ser nulo");
        this.acao = acao;
        this.senderId = senderId;
        this.senderNome = senderNome;
        this.destinoId = destinoId;
        this.grupoId = grupoId;
        this.mensagem = mensagem;
        this.itens = itens == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(itens));
        this.correlacaoId = correlacaoId;
        this.resposta = resposta;
    }

    public static ChatPacket request(Message mensagem) {
        Objects.requireNonNull(mensagem, "mensagem não pode ser nula");
        return new ChatPacket(Tipo.REQUEST, Acao.PRIVATE_MESSAGE, mensagem.getRemetenteId(), mensagem.getRemetenteNome(), null, null, mensagem, null, mensagem.getId(), null);
    }

    public static ChatPacket requestBroadcastMessage(Message mensagem) {
        Objects.requireNonNull(mensagem, "mensagem não pode ser nula");
        return new ChatPacket(Tipo.REQUEST, Acao.BROADCAST, mensagem.getRemetenteId(), mensagem.getRemetenteNome(), null, null, mensagem, null, mensagem.getId(), null);
    }

    public static ChatPacket reply(String correlacaoId, String resposta) {
        return new ChatPacket(Tipo.REPLY, Acao.SYSTEM, null, null, null, null, null, null, Objects.requireNonNull(correlacaoId, "correlacaoId não pode ser nulo"), Objects.requireNonNull(resposta, "resposta não pode ser nula"));
    }

    public static ChatPacket requestRegister(User usuario) {
        Objects.requireNonNull(usuario, "usuario não pode ser nulo");
        return new ChatPacket(Tipo.REQUEST, Acao.REGISTER, usuario.getId(), usuario.getNome(), null, null, null, null, UUID.randomUUID().toString(), null);
    }

    public static ChatPacket requestListUsers(String senderId) {
        return new ChatPacket(Tipo.REQUEST, Acao.LIST_USERS, senderId, null, null, null, null, null, UUID.randomUUID().toString(), null);
    }

    public static ChatPacket requestListGroups(String senderId) {
        return new ChatPacket(Tipo.REQUEST, Acao.LIST_GROUPS, senderId, null, null, null, null, null, UUID.randomUUID().toString(), null);
    }

    public static ChatPacket requestJoinGroup(String senderId, String grupoId) {
        return new ChatPacket(Tipo.REQUEST, Acao.JOIN_GROUP, senderId, null, null, Objects.requireNonNull(grupoId, "grupoId não pode ser nulo"), null, null, UUID.randomUUID().toString(), null);
    }

    public static ChatPacket requestDisconnect(String senderId) {
        return new ChatPacket(Tipo.REQUEST, Acao.DISCONNECT, Objects.requireNonNull(senderId, "senderId não pode ser nulo"), null, null, null, null, null, UUID.randomUUID().toString(), null);
    }

    public static ChatPacket requestPrivateMessage(Message mensagem, String destinoId) {
        Objects.requireNonNull(mensagem, "mensagem não pode ser nula");
        return new ChatPacket(Tipo.REQUEST, Acao.PRIVATE_MESSAGE, mensagem.getRemetenteId(), mensagem.getRemetenteNome(), Objects.requireNonNull(destinoId, "destinoId não pode ser nulo"), null, mensagem, null, mensagem.getId(), null);
    }

    public static ChatPacket requestGroupMessage(Message mensagem, String grupoId) {
        Objects.requireNonNull(mensagem, "mensagem não pode ser nula");
        return new ChatPacket(Tipo.REQUEST, Acao.GROUP_MESSAGE, mensagem.getRemetenteId(), mensagem.getRemetenteNome(), null, Objects.requireNonNull(grupoId, "grupoId não pode ser nulo"), mensagem, null, mensagem.getId(), null);
    }

    public static ChatPacket responseAck(String correlacaoId, String texto) {
        return new ChatPacket(Tipo.REPLY, Acao.SYSTEM, null, null, null, null, null, null, Objects.requireNonNull(correlacaoId, "correlacaoId não pode ser nulo"), Objects.requireNonNull(texto, "texto não pode ser nulo"));
    }

    public static ChatPacket responseListUsers(String correlacaoId, List<String> usuarios) {
        return new ChatPacket(Tipo.REPLY, Acao.LIST_USERS, null, null, null, null, null, Objects.requireNonNull(usuarios, "usuarios não pode ser nulo"), Objects.requireNonNull(correlacaoId, "correlacaoId não pode ser nulo"), null);
    }

    public static ChatPacket responseListGroups(String correlacaoId, List<String> grupos) {
        return new ChatPacket(Tipo.REPLY, Acao.LIST_GROUPS, null, null, null, null, null, Objects.requireNonNull(grupos, "grupos não pode ser nulo"), Objects.requireNonNull(correlacaoId, "correlacaoId não pode ser nulo"), null);
    }

    public static ChatPacket responseError(String correlacaoId, String texto) {
        return new ChatPacket(Tipo.REPLY, Acao.SYSTEM, null, null, null, null, null, null, Objects.requireNonNull(correlacaoId, "correlacaoId não pode ser nulo"), Objects.requireNonNull(texto, "texto não pode ser nulo"));
    }

    public static ChatPacket eventPrivateMessage(Message mensagem, String destinoId) {
        Objects.requireNonNull(mensagem, "mensagem não pode ser nula");
        return new ChatPacket(Tipo.REPLY, Acao.PRIVATE_MESSAGE, mensagem.getRemetenteId(), mensagem.getRemetenteNome(), Objects.requireNonNull(destinoId, "destinoId não pode ser nulo"), null, mensagem, null, null, null);
    }

    public static ChatPacket eventBroadcastMessage(Message mensagem) {
        Objects.requireNonNull(mensagem, "mensagem não pode ser nula");
        return new ChatPacket(Tipo.REPLY, Acao.BROADCAST, mensagem.getRemetenteId(), mensagem.getRemetenteNome(), null, null, mensagem, null, null, null);
    }

    public static ChatPacket eventGroupMessage(Message mensagem, String grupoId) {
        Objects.requireNonNull(mensagem, "mensagem não pode ser nula");
        return new ChatPacket(Tipo.REPLY, Acao.GROUP_MESSAGE, mensagem.getRemetenteId(), mensagem.getRemetenteNome(), null, Objects.requireNonNull(grupoId, "grupoId não pode ser nulo"), mensagem, null, null, null);
    }

    public static ChatPacket eventSystem(String texto) {
        return new ChatPacket(Tipo.REPLY, Acao.SYSTEM, null, null, null, null, null, null, null, Objects.requireNonNull(texto, "texto não pode ser nulo"));
    }

    public Tipo getTipo() {
        return tipo;
    }

    public Acao getAcao() {
        return acao;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderNome() {
        return senderNome;
    }

    public String getDestinoId() {
        return destinoId;
    }

    public String getGrupoId() {
        return grupoId;
    }

    public Message getMensagem() {
        return mensagem;
    }

    public List<String> getItens() {
        return itens;
    }

    public String getCorrelacaoId() {
        return correlacaoId;
    }

    public String getResposta() {
        return resposta;
    }

    public boolean isRequest() {
        return tipo == Tipo.REQUEST;
    }

    public boolean isReply() {
        return tipo == Tipo.REPLY;
    }
}
