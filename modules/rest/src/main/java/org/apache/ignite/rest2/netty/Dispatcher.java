package org.apache.ignite.rest2.netty;

import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class Dispatcher {

    public List<Route> routes;

    public ObjectMapper objectMapper = new ObjectMapper();

    public Dispatcher(List<Route> routes) {
        this.routes = routes;
    }

    public void route(Route route) {
        routes.add(route);
    }

    public FullHttpResponse dispatch(FullHttpRequest req) {
        var maybeRoute = routes.stream().filter(r -> r.isApplicable(req)).findFirst();
        if (maybeRoute.isPresent()) {
            try {
                var resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, OK);
                var r = new HttpRequestWithParams(req, maybeRoute.get().paramsDecode(req.uri()));
                var content = maybeRoute.get().handler.handle(r, resp);
                return new DefaultFullHttpResponse(resp.protocolVersion(), resp.status(),
                    content != null ? Unpooled.wrappedBuffer(objectMapper.writeValueAsBytes(content)) : new EmptyByteBuf(UnpooledByteBufAllocator.DEFAULT), resp.headers(), EmptyHttpHeaders.INSTANCE);
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        else
            return new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.NOT_FOUND);
    }
}
