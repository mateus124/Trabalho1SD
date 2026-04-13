package br.ufc.quixada.chat.client;

import br.ufc.quixada.chat.core.model.ChatPacket;
import br.ufc.quixada.chat.core.model.Message;
import br.ufc.quixada.chat.core.model.MessageInputStream;
import br.ufc.quixada.chat.core.model.MessageOutputStream;
import br.ufc.quixada.chat.core.model.User;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChatClient {
    private static final String HOST_PADRAO = "localhost";
    private static final int PORTA_PADRAO = 12345;
    private static final long TIMEOUT_SEGUNDOS = 5L;
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_MAGENTA = "\u001B[35m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_WHITE = "\u001B[37m";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String host = args.length > 0 ? args[0] : HOST_PADRAO;
        int porta = args.length > 1 ? Integer.parseInt(args[1]) : PORTA_PADRAO;

        System.out.println(colorir("=== BEM-VINDO AO M2TALK ===", ANSI_BOLD + ANSI_CYAN));
        System.out.print(colorir("Digite seu nome: ", ANSI_WHITE));
        String nome = scanner.nextLine();
        User usuarioAtivo = new User(nome);
        Map<String, CompletableFuture<ChatPacket>> pendencias = new ConcurrentHashMap<>();
        boolean executando = true;

        try (Socket socket = new Socket(host, porta);
             MessageOutputStream output = new MessageOutputStream(socket.getOutputStream());
             MessageInputStream input = new MessageInputStream(socket.getInputStream())) {

            Thread listenerThread = new Thread(() -> ouvirMensagens(input, pendencias, usuarioAtivo, socket));
            listenerThread.setDaemon(true);
            listenerThread.start();

            ChatPacket respostaRegistro = enviarEEsperar(output, pendencias, ChatPacket.requestRegister(usuarioAtivo));
            imprimirServidor(respostaRegistro.getResposta());

            while (executando) {
                mostrarMenu();
                String opcao = scanner.nextLine().trim();

                switch (opcao) {
                    case "1":
                        enviarBroadcast(scanner, output, usuarioAtivo);
                        break;
                    case "2":
                        listarConectados(output, pendencias, usuarioAtivo);
                        break;
                    case "3":
                        abrirChatPrivado(scanner, output, pendencias, usuarioAtivo);
                        break;
                    case "4":
                        listarGrupos(output, pendencias, usuarioAtivo);
                        break;
                    case "5":
                        entrarEmGrupo(scanner, output, pendencias, usuarioAtivo);
                        break;
                    case "6":
                        executarDesconexao(output, pendencias, usuarioAtivo);
                        executando = false;
                        break;
                    default:
                        imprimirErro("Opção inválida.");
                        break;
                }
            }

        } catch (IOException e) {
            imprimirErro("Erro ao conectar ao servidor: Verifique se o ChatServer está rodando.");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            imprimirErro("Erro ao trocar mensagens com o servidor: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println(colorir("1. Enviar broadcast para todos", ANSI_WHITE));
        System.out.println(colorir("2. Ver quem esta conectado", ANSI_WHITE));
        System.out.println(colorir("3. Abrir chat privado", ANSI_WHITE));
        System.out.println(colorir("4. Ver grupos disponiveis", ANSI_WHITE));
        System.out.println(colorir("5. Entrar em um grupo", ANSI_WHITE));
        System.out.println(colorir("6. Sair", ANSI_WHITE));
        System.out.print(colorir("Escolha: ", ANSI_BOLD + ANSI_CYAN));
    }

    private static void ouvirMensagens(MessageInputStream input, Map<String, CompletableFuture<ChatPacket>> pendencias, User usuarioAtivo, Socket socket) {
        try {
            while (!socket.isClosed()) {
                ChatPacket pacote = input.lerPacote();

                if (pacote == null) {
                    break;
                }

                if (pacote.getCorrelacaoId() != null) {
                    CompletableFuture<ChatPacket> futuraResposta = pendencias.remove(pacote.getCorrelacaoId());
                    if (futuraResposta != null) {
                        futuraResposta.complete(pacote);
                        continue;
                    }
                }

                exibirPacoteRecebido(pacote, usuarioAtivo);
            }
        } catch (IOException | ClassNotFoundException e) {
            if (!socket.isClosed()) {
                imprimirErro("Conexão com o servidor encerrada.");
            }
        }
    }

    private static void exibirPacoteRecebido(ChatPacket pacote, User usuarioAtivo) {
        if (pacote.getMensagem() != null) {
            Message mensagem = pacote.getMensagem();

            switch (pacote.getAcao()) {
                case BROADCAST:
                    imprimirBroadcast(mensagem.getRemetenteNome(), mensagem.getConteudo());
                    break;
                case PRIVATE_MESSAGE:
                    if (usuarioAtivo.getId().equals(pacote.getDestinoId())) {
                        imprimirPrivado("de " + mensagem.getRemetenteNome(), mensagem.getConteudo());
                    } else {
                        imprimirPrivado("para " + pacote.getDestinoId(), mensagem.getConteudo());
                    }
                    break;
                case GROUP_MESSAGE:
                    imprimirGrupo(pacote.getGrupoId(), mensagem.getRemetenteNome(), mensagem.getConteudo());
                    break;
                default:
                    imprimirSistema("[Mensagem] " + mensagem.getRemetenteNome() + ": " + mensagem.getConteudo());
                    break;
            }
            return;
        }

        if (pacote.getResposta() != null) {
            imprimirSistema(pacote.getResposta());
        }
    }

    private static void enviarBroadcast(Scanner scanner, MessageOutputStream output, User usuarioAtivo) throws IOException {
        System.out.print(colorir("Mensagem do broadcast: ", ANSI_BOLD + ANSI_YELLOW));
        String texto = scanner.nextLine();
        if (texto.isBlank()) {
            return;
        }

        Message mensagem = new Message(usuarioAtivo.getId(), usuarioAtivo.getNome(), texto);
        output.enviarPacote(ChatPacket.requestBroadcastMessage(mensagem));
    }

    private static void listarConectados(MessageOutputStream output, Map<String, CompletableFuture<ChatPacket>> pendencias, User usuarioAtivo) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        ChatPacket resposta = enviarEEsperar(output, pendencias, ChatPacket.requestListUsers(usuarioAtivo.getId()));
        List<String> usuarios = resposta.getItens();

        System.out.println("\n" + colorir("=== Conectados ===", ANSI_BOLD + ANSI_CYAN));
        for (String entrada : usuarios) {
            String[] partes = entrada.split("\\|", 2);
            if (partes.length == 2) {
                System.out.println(colorir(partes[1] + " - " + partes[0], ANSI_WHITE));
            }
        }
    }

    private static void listarGrupos(MessageOutputStream output, Map<String, CompletableFuture<ChatPacket>> pendencias, User usuarioAtivo) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        ChatPacket resposta = enviarEEsperar(output, pendencias, ChatPacket.requestListGroups(usuarioAtivo.getId()));
        List<String> grupos = resposta.getItens();

        System.out.println("\n" + colorir("=== Grupos ===", ANSI_BOLD + ANSI_CYAN));
        for (String entrada : grupos) {
            String[] partes = entrada.split("\\|", 2);
            if (partes.length == 2) {
                System.out.println(colorir(partes[0] + " - " + partes[1] + " membros", ANSI_WHITE));
            }
        }
    }

    private static void abrirChatPrivado(Scanner scanner, MessageOutputStream output, Map<String, CompletableFuture<ChatPacket>> pendencias, User usuarioAtivo) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        ChatPacket resposta = enviarEEsperar(output, pendencias, ChatPacket.requestListUsers(usuarioAtivo.getId()));
        List<EntradaLista> usuarios = converterUsuarios(resposta.getItens(), usuarioAtivo.getId());

        if (usuarios.isEmpty()) {
            imprimirSistema("Nenhum outro usuario conectado.");
            return;
        }

        EntradaLista selecionado = selecionarEntrada(scanner, usuarios, "Escolha o usuario para chat privado");
        if (selecionado == null) {
            return;
        }

        conversar(scanner, output, usuarioAtivo, "Privado com " + selecionado.nome, (texto, mensagem) ->
                ChatPacket.requestPrivateMessage(mensagem, selecionado.id));
    }

    private static void entrarEmGrupo(Scanner scanner, MessageOutputStream output, Map<String, CompletableFuture<ChatPacket>> pendencias, User usuarioAtivo) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        ChatPacket resposta = enviarEEsperar(output, pendencias, ChatPacket.requestListGroups(usuarioAtivo.getId()));
        List<EntradaLista> grupos = converterGrupos(resposta.getItens());

        if (grupos.isEmpty()) {
            imprimirSistema("Nenhum grupo disponivel.");
            return;
        }

        EntradaLista selecionado = selecionarEntrada(scanner, grupos, "Escolha o grupo para entrar");
        if (selecionado == null) {
            return;
        }

        ChatPacket respostaEntrada = enviarEEsperar(output, pendencias, ChatPacket.requestJoinGroup(usuarioAtivo.getId(), selecionado.id));
        imprimirServidor(respostaEntrada.getResposta());

        conversar(scanner, output, usuarioAtivo, "Grupo " + selecionado.nome, (texto, mensagem) ->
                ChatPacket.requestGroupMessage(mensagem, selecionado.id));
    }

    private static void conversar(Scanner scanner, MessageOutputStream output, User usuarioAtivo, String titulo, CriadorPacote criadorPacote) throws IOException {
        System.out.println("\n" + colorir("=== " + titulo + " ===", ANSI_BOLD + ANSI_CYAN));
        System.out.println(colorir("Digite uma mensagem ou /voltar, voltar, sair ou 0 para retornar ao menu.", ANSI_WHITE));

        while (true) {
            System.out.print(colorir(titulo + ": ", ANSI_BOLD + ANSI_CYAN));
            String texto = scanner.nextLine();
            String comando = texto.trim();

            if (ehComandoDeRetorno(comando)) {
                imprimirSistema("Retornando ao menu principal...");
                return;
            }

            if (comando.isBlank()) {
                continue;
            }

            Message mensagem = new Message(usuarioAtivo.getId(), usuarioAtivo.getNome(), texto);
            output.enviarPacote(criadorPacote.criar(texto, mensagem));
        }
    }

    private static void executarDesconexao(MessageOutputStream output, Map<String, CompletableFuture<ChatPacket>> pendencias, User usuarioAtivo) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        enviarEEsperar(output, pendencias, ChatPacket.requestDisconnect(usuarioAtivo.getId()));
    }

    private static ChatPacket enviarEEsperar(MessageOutputStream output, Map<String, CompletableFuture<ChatPacket>> pendencias, ChatPacket pedido) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        CompletableFuture<ChatPacket> futuro = new CompletableFuture<>();
        pendencias.put(pedido.getCorrelacaoId(), futuro);
        output.enviarPacote(pedido);

        try {
            return futuro.get(TIMEOUT_SEGUNDOS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pendencias.remove(pedido.getCorrelacaoId());
            throw e;
        }
    }

    private static List<EntradaLista> converterUsuarios(List<String> entradas, String usuarioAtualId) {
        List<EntradaLista> usuarios = new ArrayList<>();

        for (String entrada : entradas) {
            String[] partes = entrada.split("\\|", 2);
            if (partes.length == 2 && !usuarioAtualId.equals(partes[0])) {
                usuarios.add(new EntradaLista(partes[0], partes[1]));
            }
        }

        return usuarios;
    }

    private static List<EntradaLista> converterGrupos(List<String> entradas) {
        List<EntradaLista> grupos = new ArrayList<>();

        for (String entrada : entradas) {
            String[] partes = entrada.split("\\|", 2);
            if (partes.length == 2) {
                grupos.add(new EntradaLista(partes[0], partes[0] + " (" + partes[1] + " membros)"));
            }
        }

        return grupos;
    }

    private static EntradaLista selecionarEntrada(Scanner scanner, List<EntradaLista> entradas, String titulo) {
        System.out.println("\n" + colorir("=== " + titulo + " ===", ANSI_BOLD + ANSI_CYAN));
        for (int i = 0; i < entradas.size(); i++) {
            EntradaLista entrada = entradas.get(i);
            System.out.println(colorir((i + 1) + ". " + entrada.nome, ANSI_WHITE));
        }
        System.out.print(colorir("Escolha um numero ou 0 para cancelar: ", ANSI_BOLD + ANSI_CYAN));

        String valor = scanner.nextLine().trim();
        int indice;

        try {
            indice = Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return null;
        }

        if (indice <= 0 || indice > entradas.size()) {
            return null;
        }

        return entradas.get(indice - 1);
    }

    private static class EntradaLista {
        private final String id;
        private final String nome;

        private EntradaLista(String id, String nome) {
            this.id = id;
            this.nome = nome;
        }
    }

    private static boolean ehComandoDeRetorno(String comando) {
        return "/voltar".equalsIgnoreCase(comando)
                || "voltar".equalsIgnoreCase(comando)
                || "sair".equalsIgnoreCase(comando)
                || "0".equals(comando);
    }

    private static void imprimirServidor(String mensagem) {
        System.out.println("\n" + colorir("[Servidor] " + mensagem, ANSI_GREEN));
    }

    private static void imprimirSistema(String mensagem) {
        System.out.println("\n" + colorir("[Sistema] " + mensagem, ANSI_BLUE));
    }

    private static void imprimirErro(String mensagem) {
        System.out.println("\n" + colorir("[Erro] " + mensagem, ANSI_RED));
    }

    private static void imprimirBroadcast(String remetente, String conteudo) {
        System.out.println("\n" + colorir("[Broadcast] " + remetente + ": " + conteudo, ANSI_YELLOW));
    }

    private static void imprimirPrivado(String direcao, String conteudo) {
        System.out.println("\n" + colorir("[Privado] " + direcao + ": " + conteudo, ANSI_MAGENTA));
    }

    private static void imprimirGrupo(String grupoId, String remetente, String conteudo) {
        System.out.println("\n" + colorir("[Grupo " + grupoId + "] " + remetente + ": " + conteudo, ANSI_CYAN));
    }

    private static String colorir(String texto, String cor) {
        return cor + texto + ANSI_RESET;
    }

    private interface CriadorPacote {
        ChatPacket criar(String texto, Message mensagem);
    }
}