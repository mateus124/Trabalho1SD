package br.ufc.quixada.chat.core.model;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class MessageInputStream implements Closeable {
    private final ObjectInputStream in;

    public MessageInputStream(InputStream in) throws IOException {
        this.in = new ObjectInputStream(in);
    }

    public ChatPacket lerPacote() throws IOException, ClassNotFoundException {
        return (ChatPacket) in.readObject();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
}