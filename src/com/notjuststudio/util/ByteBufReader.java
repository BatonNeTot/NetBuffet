package com.notjuststudio.util;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.io.InputStream;

public class ByteBufReader extends InputStream {

    private final ByteBuf buffer;

    public ByteBufReader(ByteBuf buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() {
        try {
            return buffer.readByte();
        } catch (IndexOutOfBoundsException e) {
            return -1;
        }
    }

    @Override
    public int available() throws IOException {
        return buffer.capacity() - buffer.readerIndex();
    }
}
