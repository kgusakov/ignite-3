package org.apache.ignite.rest2.netty;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;

public class Route {

    /** Route. */
    private final String route;

    /** Method. */
    private final HttpMethod method;

    /** Accept type. */
    private final Optional<String> acceptType;

    /** Handler. */
    private final BiConsumer<RestApiHttpRequest, RestApiHttpResponse> handler;

    public Route(String route, HttpMethod method, Optional<String> acceptType,
        BiConsumer<RestApiHttpRequest, RestApiHttpResponse> handler) {
        this.route = route;
        this.method = method;
        this.acceptType = acceptType;
        this.handler = handler;
    }

    public void handle(FullHttpRequest req, RestApiHttpResponse resp) {
        handler.accept(new RestApiHttpRequest(req, paramsDecode(req.uri())),
            resp);
    }

    public boolean isApplicable(HttpRequest req) {
        return req.method().equals(method) &&
            match(req.uri());
    }

    public boolean match(String uri) {
        var receivedParts = new ArrayDeque<>(Arrays.asList(uri.split("/")));
        var realparts = new ArrayDeque<>(Arrays.asList(route.split("/")));

        String part;
        while ((part = realparts.pollFirst()) != null) {
            String receivedPart = receivedParts.pollFirst();
            if (receivedPart == null)
                return false;

            if (part.startsWith(":"))
                continue;

            if (!part.equals(receivedPart))
                return false;
        }
        return receivedParts.isEmpty();
    }

    public Map<String, String> paramsDecode(String uri) {

        var receivedParts = new ArrayDeque<>(Arrays.asList(uri.split("/")));
        var realparts = new ArrayDeque<>(Arrays.asList(route.split("/")));

         Map<String, String> res = new HashMap<>();

        String part;
        while ((part = realparts.pollFirst()) != null) {
            String receivedPart = receivedParts.pollFirst();
            if (receivedPart == null)
                throw new IllegalArgumentException("URI is incorrect");

            if (part.startsWith(":")) {
                res.put(part.substring(1), receivedPart);
                continue;
            }

            if (!part.equals(receivedPart))
                throw new IllegalArgumentException("URI is incorrect");
        }
        return res;
    }

}
