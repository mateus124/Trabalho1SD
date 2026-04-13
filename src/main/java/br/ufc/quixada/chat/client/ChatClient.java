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

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String host = args.length > 0 ? args[0] : HOST_PADRAO;
        int porta = args.length > 1 ? Integer.parseInt(args[1]) : PORTA_PADRAO;

        System.out.println("=== BEM-VINDO AO CHAT ===");
        System.out.print("Digite seu nome para entrar: ");
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
            System.out.println("[Servidor] " + respostaRegistro.getResposta());

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
                        System.out.println("Opção inválida.");
                        break;
                }
            }

        } catch (IOException e) {
            System.out.println("Erro ao conectar ao servidor: Verifique se o ChatServer está rodando.");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            System.out.println("Erro ao trocar mensagens com o servidor: " + e.getMessage());
        } finally {
            scanner.close();
            System.out.println("Chat encerrado.");
        }
    }

    private static void mostrarMenu() {
        System.out.println();
        System.out.println("1. Enviar broadcast para todos");
        System.out.println("2. Ver quem esta conectado");
        System.out.println("3. Abrir chat privado");
        System.out.println("4. Ver grupos disponiveis");
        System.out.println("5. Entrar em um grupo");
        System.out.println("6. Sair");
        System.out.print("Escolha: ");
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
                System.out.println("\n[Aviso] Conexão com o servidor encerrada.");
            }
        }
    }

    private static void exibirPacoteRecebido(ChatPacket pacote, User usuarioAtivo) {
        if (pacote.getMensagem() != null) {
            Message mensagem = pacote.getMensagem();

            switch (pacote.getAcao()) {
                case BROADCAST:
                    System.out.println("\n[Broadcast] " + mensagem.getRemetenteNome() + ": " + mensagem.getConteudo());
                    break;
                case PRIVATE_MESSAGE:
                    if (usuarioAtivo.getId().equals(pacote.getDestinoId())) {
                        System.out.println("\n[Privado] de " + mensagem.getRemetenteNome() + ": " + mensagem.getConteudo());
                    } else {
                        System.out.println("\n[Privado] para " + pacote.getDestinoId() + ": " + mensagem.getConteudo());
                    }
                    break;
                case GROUP_MESSAGE:
                    System.out.println("\n[Grupo " + pacote.getGrupoId() + "] " + mensagem.getRemetenteNome() + ": " + mensagem.getConteudo());
                    break;
                default:
                    System.out.println("\n[Mensagem] " + mensagem.getRemetenteNome() + ": " + mensagem.getConteudo());
                    break;
            }
            return;
        }

        if (pacote.getResposta() != null) {
            System.out.println("\n[Sistema] " + pacote.getResposta());
        }
    }

    private static void enviarBroadcast(Scanner scanner, MessageOutputStream output, User usuarioAtivo) throws IOException {
        System.out.print("Mensagem do broadcast: ");
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

        System.out.println("\n=== Conectados ===");
        for (String entrada : usuarios) {
            String[] partes = entrada.split("\\|", 2);
            if (partes.length == 2) {
                System.out.println(partes[1] + " - " + partes[0]);
            }
        }
    }

    private static void listarGrupos(MessageOutputStream output, Map<String, CompletableFuture<ChatPacket>> pendencias, User usuarioAtivo) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        ChatPacket resposta = enviarEEsperar(output, pendencias, ChatPacket.requestListGroups(usuarioAtivo.getId()));
        List<String> grupos = resposta.getItens();

        System.out.println("\n=== Grupos ===");
        for (String entrada : grupos) {
            String[] partes = entrada.split("\\|", 2);
            if (partes.length == 2) {
                System.out.println(partes[0] + " - " + partes[1] + " membros");
            }
        }
    }

    private static void abrirChatPrivado(Scanner scanner, MessageOutputStream output, Map<String, CompletableFuture<ChatPacket>> pendencias, User usuarioAtivo) throws IOException, InterruptedException, ExecutionException, TimeoutException {
        ChatPacket resposta = enviarEEsperar(output, pendencias, ChatPacket.requestListUsers(usuarioAtivo.getId()));
        List<EntradaLista> usuarios = converterUsuarios(resposta.getItens(), usuarioAtivo.getId());

        if (usuarios.isEmpty()) {
            System.out.println("Nenhum outro usuario conectado.");
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
            System.out.println("Nenhum grupo disponivel.");
            return;
        }

        EntradaLista selecionado = selecionarEntrada(scanner, grupos, "Escolha o grupo para entrar");
        if (selecionado == null) {
            return;
        }

        ChatPacket respostaEntrada = enviarEEsperar(output, pendencias, ChatPacket.requestJoinGroup(usuarioAtivo.getId(), selecionado.id));
        System.out.println("[Servidor] " + respostaEntrada.getResposta());

        conversar(scanner, output, usuarioAtivo, "Grupo " + selecionado.nome, (texto, mensagem) ->
                ChatPacket.requestGroupMessage(mensagem, selecionado.id));
    }

    private static void conversar(Scanner scanner, MessageOutputStream output, User usuarioAtivo, String titulo, CriadorPacote criadorPacote) throws IOException {
        System.out.println("\n=== " + titulo + " ===");
        System.out.println("Digite uma mensagem ou /voltar, voltar, sair ou 0 para retornar ao menu.");

        while (true) {
            System.out.print(titulo + ": ");
            String texto = scanner.nextLine();
            String comando = texto.trim();

            if (ehComandoDeRetorno(comando)) {
                System.out.println("Retornando ao menu principal...");
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
        System.out.println("\n=== " + titulo + " ===");
        for (int i = 0; i < entradas.size(); i++) {
            EntradaLista entrada = entradas.get(i);
            System.out.println((i + 1) + ". " + entrada.nome);
        }
        System.out.print("Escolha um numero ou 0 para cancelar: ");

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

    private interface CriadorPacote {
        ChatPacket criar(String texto, Message mensagem);
    }
}