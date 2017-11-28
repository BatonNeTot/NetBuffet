package com.notjuststudio.netbuffet;

import com.notjuststudio.bytebun.ByteBun;
import com.notjuststudio.fpnt.FPNTContainer;
import com.notjuststudio.fpnt.FPNTDecoder;
import com.notjuststudio.fpnt.FPNTExpander;
import com.notjuststudio.secretingredient.Recipe;
import com.notjuststudio.threadsauce.LockBoolean;
import com.notjuststudio.util.ByteBunUtils;
import com.notjuststudio.util.ByteBunWriter;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;

import javax.crypto.SecretKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Connection {

    private final Channel channel;

    final Map<String, FPNTContainer> synchronizedContainers = new ConcurrentHashMap<>();
    final Map<String, HandlerContainer> handlers = new ConcurrentHashMap<>();
    HandlerConnection
            active = null,
            inactive = null;
    HandlerException
            exception = null;

    SecureBase
            secureBase = null;
    SecretKey
            secretKey = null;
    final LockBoolean wasInitialized = new LockBoolean(false);

    Set<FPNTExpander> expanders = null;

    static Connection create(@NotNull final SecureBase base, @NotNull final Channel channel, @Nullable final HanlderMapInitializer initializer, @Nullable final HandlerConnectionCreator active, @Nullable final HandlerConnectionCreator inactive, @Nullable final HandlerExceptionCreator exception) {
        final Map<String, HandlerContainer> handlerMap = new HashMap<>();
        if (initializer != null)
            initializer.createHandlerMap(handlerMap);
        return new Connection(base, channel, handlerMap, (active == null ? null : active.createHandler()), (inactive == null ? null : inactive.createHandler()), (exception == null ? null : exception.createHandler()));
    }

    private Connection(@NotNull final SecureBase base, @NotNull final Channel channel, @NotNull final Map<String, HandlerContainer> handlers, @Nullable final HandlerConnection active, @Nullable final HandlerConnection inactive, @Nullable final HandlerException exception) {
        this.channel = channel;
        this.handlers.putAll(handlers);
        this.active = active;
        this.inactive = inactive;
        this.exception = exception;
        this.secureBase = base;
    }

    public ChannelId id() {
        return channel.id();
    }

    public void addHandler(@NotNull final String key, @NotNull final HandlerContainer handler) {
        handlers.put(key, handler);
    }

    public void removeHandler(@NotNull final String key) {
        handlers.remove(key);
    }

    void expanders(@NotNull final Set<FPNTExpander> expanders) {
        this.expanders = expanders;
    }

    Set<FPNTExpander> expanders() {
        return expanders;
    }

    public void active(HandlerConnection active) {
        this.active = active;
    }

    public HandlerConnection active() {
        return active;
    }

    public void inactive(HandlerConnection inactive) {
        this.inactive = inactive;
    }

    public HandlerConnection inactive() {
        return inactive;
    }

    public void exception(HandlerException exception) {
        this.exception = exception;
    }

    public HandlerException exception() {
        return exception;
    }

    public boolean isAlive() {
        return channel.isActive();
    }

    public void close() {
        channel.close();
    }

    public void send(@NotNull final String target, @NotNull final FPNTContainer container) {
        if (!channel.isActive())
            return;

        final ByteBun buffer = ByteBun.allocate(1);
        buffer.writeBoolean(false);
        ByteBunUtils.writeString(target, buffer, true);
        final ByteBunWriter writer = new ByteBunWriter(buffer);
        FPNTDecoder.encode(writer, container);
        writer.flush();
        send(buffer);
    }

    private void send(@NotNull final ByteBun message) {
        if (secureBase.cryptoProtective.get()) {
            final byte[] source = new byte[message.writerIndex()];
            message.readBytes(source);
            final byte[] cipher = Recipe.encryptAES(secretKey, source);
            final ByteBun result = ByteBun.allocate(cipher.length + 4);
            result.writeInt(cipher.length);
            result.writeBytes(cipher);
            channel.writeAndFlush(ByteBunUtils.createBuf(result));
        } else {
            channel.writeAndFlush(ByteBunUtils.createBuf(message));
        }
    }

    private void send(@NotNull final String name, @NotNull final byte type, @NotNull final String key) {
        final byte[] nameSource = name.getBytes();
        final byte[] keySource = key.getBytes();
        final ByteBun buffer = ByteBun.allocate(nameSource.length + keySource.length + 10);

        buffer.writeBoolean(true);
        ByteBunUtils.writeString(name, buffer);
        buffer.writeByte(type);
        ByteBunUtils.writeString(key, buffer);
        final FPNTContainer container = synchronizedContainers.get(name);
        if (container.contains(type, key)) {
            final ByteBunWriter writer = new ByteBunWriter(buffer);
            FPNTDecoder.encode(writer, container, type, key);
            writer.flush();
        }
        send(buffer);
    }

    void createContainer(@NotNull final String name) {
        final FPNTContainer newContainer = new FPNTContainer();

        synchronizedContainers.put(name, newContainer);

        newContainer.addHandler((container, type, key, old, value) -> {
            for (Map.Entry<String, FPNTContainer> entry : synchronizedContainers.entrySet()) {
                if (entry.getValue() == newContainer) {
                    send(entry.getKey(), type, key);
                    break;
                }
            }
        });
    }

    public FPNTContainer getContainer(@NotNull final String name) {
        if (!synchronizedContainers.containsKey(name))
            createContainer(name);
        return synchronizedContainers.get(name);
    }

}
