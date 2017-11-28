package com.notjuststudio.util;

import com.notjuststudio.bytebun.ByteBun;
import com.sun.istack.internal.NotNull;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ByteBunUtils {

    public static String readString(@NotNull final ByteBun buffer) {
        final int length = buffer.readInt();
        final byte[] source = new byte[length];
        buffer.readBytes(source);
        return new String(source);
    }

    public static void writeString(@NotNull final String string, @NotNull final ByteBun buffer) {
        writeString(string, buffer, false);
    }

    public static void writeString(@NotNull final String string, @NotNull final ByteBun buffer, @NotNull final boolean expand) {
        final byte[] source = string.getBytes();
        if (expand) {
            buffer.capacity(buffer.capacity() + source.length + 4);
        }
        buffer.writeInt(source.length);
        buffer.writeBytes(source);
    }

    public static ByteBun createBun(@NotNull final ByteBuf buf) {
        final ByteBun bun = ByteBun.allocate(buf.capacity());
        final byte[] tmp = new byte[buf.capacity()];
        buf.getBytes(0, tmp);
        bun.writeBytes(tmp);
        return bun;
    }

    public static ByteBuf createBuf(@NotNull final ByteBun bun) {
        final ByteBuf buf = Unpooled.buffer(bun.capacity(), bun.capacity());
        final byte[] tmp = new byte[bun.capacity()];
        bun.getBytes(0, tmp);
        buf.writeBytes(tmp);
        return buf;
    }
}
