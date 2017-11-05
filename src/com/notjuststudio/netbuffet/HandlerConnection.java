package com.notjuststudio.netbuffet;

import com.sun.istack.internal.Nullable;

public interface HandlerConnection {

    void handle(@Nullable final Connection connection);
}
