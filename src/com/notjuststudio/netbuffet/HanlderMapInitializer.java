package com.notjuststudio.netbuffet;

import com.sun.istack.internal.NotNull;

import java.util.Map;

public interface HanlderMapInitializer {

    void createHandlerMap(@NotNull final Map<String, HandlerContainer> handlerMap);

}
