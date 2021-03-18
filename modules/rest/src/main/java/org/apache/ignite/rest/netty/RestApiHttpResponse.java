package org.apache.ignite.rest.netty;

import java.nio.charset.StandardCharsets;
import com.google.gson.Gson;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

public class RestApiHttpResponse {

    private final HttpResponse response;

    private byte[] content;

    public RestApiHttpResponse(HttpResponse response, byte[] content) {
        this.response = response;
        this.content = content;
    }

    public RestApiHttpResponse(HttpResponse response) {
        this.response = response;
    }

    public RestApiHttpResponse content(byte[] content) {
        this.content = content;
        return this;
    }

    public RestApiHttpResponse json(Object content) {
        this.content = new Gson().toJson(content).getBytes(StandardCharsets.UTF_8);
        headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON.toString());
        return this;
    }

    public byte[] content() {
        return content;
    }

    public HttpResponseStatus status() {
        return response.status();
    }

    public RestApiHttpResponse status(HttpResponseStatus status) {
        response.setStatus(status);
        return this;
    }

    public HttpVersion protocolVersion() {
        return response.protocolVersion();
    }

    public RestApiHttpResponse protocolVersion(HttpVersion httpVersion) {
        response.setProtocolVersion(httpVersion);
        return this;
    }

    public HttpHeaders headers() {
        return response.headers();
    }
}
