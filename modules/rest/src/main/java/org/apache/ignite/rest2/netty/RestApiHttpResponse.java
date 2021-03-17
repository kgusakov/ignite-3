package org.apache.ignite.rest2.netty;

import io.netty.handler.codec.http.HttpResponse;

public class RestApiHttpResponse {

    private final HttpResponse response;

    private Object content;

    public RestApiHttpResponse(HttpResponse response, Object content) {
        this.response = response;
        this.content = content;
    }

    public RestApiHttpResponse(HttpResponse response) {
        this.response = response;
    }

    public void content(Object content) {
        this.content = content;
    }

    public Object content() {
        return content;
    }

    public HttpResponse response() {
        return response;
    }
}
