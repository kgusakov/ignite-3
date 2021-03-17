package org.apache.ignite.rest2.netty;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AsciiString;

public class Router {

    /** Routes. */
    public List<Route> routes;

    /**
     * @param routes Routes.
     */
    public Router(List<Route> routes) {
        this.routes = routes;
    }

    public static Route get(String route, AsciiString acceptType, BiConsumer<RestApiHttpRequest, RestApiHttpResponse> handler) {
        return new Route(route, HttpMethod.GET, Optional.of(acceptType.toString()), handler);
    }

    public static Route get(String route, BiConsumer<RestApiHttpRequest, RestApiHttpResponse> handler) {
        return new Route(route, HttpMethod.GET, Optional.empty(), handler);
    }

    public static Route put(String route, AsciiString acceptType, BiConsumer<RestApiHttpRequest, RestApiHttpResponse> handler) {
        return new Route(route, HttpMethod.PUT, Optional.ofNullable(acceptType.toString()), handler);
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public Optional<Route> route(HttpRequest req) {
        return routes.stream().filter(r -> r.isApplicable(req)).findFirst();
    }
}
