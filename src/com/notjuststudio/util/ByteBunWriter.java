package com.notjuststudio.util;

import com.notjuststudio.bytebun.ByteBun;

import java.io.OutputStream;

public class ByteBunWriter  extends OutputStream {

    private final ByteBun target;

    private int count = 0;
    private final int size = 8192;
    private final byte[] buffer = new byte[size];

    public ByteBunWriter(ByteBun buffer) {
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
