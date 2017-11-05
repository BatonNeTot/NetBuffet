package com.notjuststudio.netbuffet;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

public interface HandlerException {

    void handle(@Nullable final Connection connection, @NotNull final Throwable throwable);
}
