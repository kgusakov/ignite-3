package org.apache.ignite.rest2.netty;

import io.netty.handler.codec.http.HttpResponse;

public interface Handler {
    Object handle(HttpRequestWithParams req, HttpResponse resp);
}
