package com.notjuststudio.util;

import com.notjuststudio.bytebun.ByteBun;

import java.io.IOException;
import java.io.InputStream;

public class ByteBunReader extends InputStream {

    private final ByteBun buffer;

    public ByteBunReader(ByteBun buffer) {
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
        return buffer.writerIndex() - buffer.readerIndex();
    }
}
