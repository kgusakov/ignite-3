package org.apache.ignite.rest2.netty;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AsciiString;

public class Route {

    public final String route;

    public final HttpMethod method;

    public final Optional<String> acceptType;

    public final Handler handler;

    private Route(String route, HttpMethod method, Optional<String> acceptType,
        Handler handler) {
        this.route = route;
        this.method = method;
        this.acceptType = acceptType;
        this.handler = handler;
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

    public static <T> Route get(String route, AsciiString acceptType, Handler handler) {
        return new Route(route, HttpMethod.GET, Optional.of(acceptType.toString()), handler);
    }

    public static <T> Route get(String route, Handler handler) {
        return new Route(route, HttpMethod.GET, Optional.empty(), handler);
    }

    public static Route put(String route, AsciiString acceptType, Handler handler) {
        return new Route(route, HttpMethod.PUT, Optional.ofNullable(acceptType.toString()), handler);
    }
}
