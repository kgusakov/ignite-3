package org.apache.ignite.rest.routes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.AsciiString;
import org.apache.ignite.rest.netty.RestApiHttpRequest;
import org.apache.ignite.rest.netty.RestApiHttpResponse;

public class Router {

    /** Routes. */
    public List<Route> routes;

    /**
     * @param routes Routes.
     */
    public Router(List<Route> routes) {
        this.routes = routes;
    }

    public Router() {
        routes = new ArrayList<>();
    }

    public Router get(String route, AsciiString acceptType, BiConsumer<RestApiHttpRequest, RestApiHttpResponse> handler) {
        addRoute(new Route(route, HttpMethod.GET, acceptType.toString(), handler));
        return this;
    }

    public Router get(String route, BiConsumer<RestApiHttpRequest, RestApiHttpResponse> handler) {
        addRoute(new Route(route, HttpMethod.GET, null, handler));
        return this;
    }

    public Router put(String route, AsciiString acceptType, BiConsumer<RestApiHttpRequest, RestApiHttpResponse> handler) {
        addRoute(new Route(route, HttpMethod.PUT, acceptType.toString(), handler));
        return this;
    }

    public void addRoute(Route route) {
        routes.add(route);
    }

    public Optional<Route> route(HttpRequest req) {
        return routes.stream().filter(r -> r.match(req)).findFirst();
    }
}
