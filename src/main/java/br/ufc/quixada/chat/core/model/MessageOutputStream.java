package br.ufc.quixada.chat.core.model;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class MessageOutputStream implements Closeable {
    private final ObjectOutputStream out;

    public MessageOutputStream(OutputStream out) throws IOException {
        this.out = new ObjectOutputStream(out);
        this.out.flush();
    }

    public synchronized void enviarPacote(ChatPacket pacote) throws IOException {
        out.writeObject(pacote);
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}