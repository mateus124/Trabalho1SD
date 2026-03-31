package br.ufc.quixada.chat.core.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public class TesteStreams {
    public static void main(String[] args) throws Exception {
        Message m1 = new Message("user1", "Chico", "Olá, tudo bem?");
        Message m2 = new Message("user2", "Dodó", "Tudo certo!");
        Message[] mensagens = {m1, m2};

        int[] tamanhos = {36, 36, 64};

        System.out.println("--- TESTE 1: Gravando e lendo em arquivo ---");
        
        try (FileOutputStream fileOut = new FileOutputStream("dados_chat.bin")) {
            MessageOutputStream customOut = new MessageOutputStream(mensagens, 2, tamanhos, fileOut);
            customOut.enviarDados();    
            System.out.println("Dados salvos em dados_chat.bin!");
        }

        try (FileInputStream fileIn = new FileInputStream("dados_chat.bin")) {
            MessageInputStream customIn = new MessageInputStream(fileIn);
            List<Message> lidas = customIn.lerDados(tamanhos);
            
            System.out.println("Mensagens lidas do arquivo:");
            lidas.forEach(msg -> System.out.println("Enviado por " + msg.getRemetenteId() + ": " + msg.getConteudo()));
        }

        System.out.println("\n--- TESTE 2: Saída padrão (System.out) ---");
        MessageOutputStream customOutConsole = new MessageOutputStream(mensagens, 2, tamanhos, System.out);
        customOutConsole.enviarDados();
        System.out.println("\nDados enviados para o console!");

        System.out.println("\n--- TESTE 3: Leitura simulada de System.in ---");
        System.out.println("Dica: Para ler do System.in de verdade, você precisaria digitar exatamente os blocos de bytes.");
        System.out.println("No teste real, passamos 'System.in' no construtor de MessageInputStream.");


        System.out.println("\n--- TESTE 4: Servidor Remoto (TCP Socket) ---");
        System.out.println("Exemplo de como passar o socket:");
        System.out.println("// No cliente (Item 5 do checklist):");
        System.out.println("// Socket socket = new Socket(\"localhost\", 12345);");
        System.out.println("// MessageOutputStream outRede = new MessageOutputStream(mensagens, 2, tamanhos, socket.getOutputStream());");
        System.out.println("// outRede.enviarDados();");

        System.out.println("\n--- TESTE 3: Lendo de System.in (Entrada Padrão) ---");
        System.out.println("Aguardando bytes via System.in...");


        MessageInputStream inputPadrao = new MessageInputStream(System.in);
        try {
            List<Message> lidasIn = inputPadrao.lerDados(tamanhos);
        if (!lidasIn.isEmpty()) {
        System.out.println("Sucesso! Mensagens lidas do System.in:");
        lidasIn.forEach(m -> System.out.println(" -> " + m.getConteudo()));
        } else {
        System.out.println("Nenhum dado lido. (Certifique-se de usar o '<' no terminal)");
    }
} catch (Exception e) {
    System.out.println("Fim da leitura ou erro no buffer.");
}
    }
}
