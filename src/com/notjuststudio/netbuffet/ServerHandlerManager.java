package com.notjuststudio.netbuffet;

import com.sun.istack.internal.NotNull;
import io.netty.channel.ChannelHandlerContext;

class ServerHandlerManager extends HandlerManager {

    private final Server SERVER;

    ServerHandlerManager(@NotNull final Server server, @NotNull final Connection connection) {
        super(connection, server.printExceptions.get());
        this.SERVER = server;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        SERVER.count.ifDoElse((value) -> SERVER.threshold.get() == 0 || value < SERVER.threshold.get(),
                value -> {
                    SERVER.CONNECTIONS.add(CONNECTION);
                    return value;
                },
                value -> {
                    ctx.close();
                    return value;
                });
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        SERVER.count.doWith(value -> {
            SERVER.CONNECTIONS.remove(CONNECTION);
            return --value;
        });
    }
}
