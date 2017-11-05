package com.notjuststudio.util;

import io.netty.buffer.ByteBuf;

import java.io.OutputStream;

public class ByteBufWriter extends OutputStream {

    private final ByteBuf target;

    private int count = 0;
    private final int size = 8192;
    private final byte[] buffer = new byte[size];

    public ByteBufWriter(ByteBuf buffer) {
        this.target = buffer;
    }

    @Override
    public void write(int b) {
        if (count >= size)
            flush();
        buffer[count++] = (byte)b;
    }

    @Override
    public void flush() {
        target.capacity(target.capacity() + count);
        target.writeBytes(buffer, 0, count);
        count = 0;
    }
}
