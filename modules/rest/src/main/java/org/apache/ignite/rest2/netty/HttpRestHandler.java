package org.apache.ignite.rest2.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class HttpRestHandler extends SimpleChannelInboundHandler<HttpObject> {

    /** Dispatcher. */
    private final Router router;

    /** Object mapper. */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param router Dispatcher.
     */
    public HttpRestHandler(Router router) {
        this.router = router;
    }

    /** {@inheritDoc} */
    @Override public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /** {@inheritDoc} */
    @Override protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;

            boolean keepAlive = HttpUtil.isKeepAlive(req);
            FullHttpResponse res;
            var maybeRoute = router.route(req);
            if (maybeRoute.isPresent()) {
                var resp = new RestApiHttpResponse(new DefaultHttpResponse(HttpVersion.HTTP_1_1, OK));
                maybeRoute.get().handle(req, resp);
                var content = resp.content() != null ?
                    Unpooled.wrappedBuffer(objectMapper.writeValueAsBytes(resp.content())) :
                    new EmptyByteBuf(UnpooledByteBufAllocator.DEFAULT);
                res = new DefaultFullHttpResponse(resp.response().protocolVersion(), resp.response().status(),
                     content, resp.response().headers(), EmptyHttpHeaders.INSTANCE);
            }
            else
                res = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.NOT_FOUND);

            res.headers()
                .setInt(CONTENT_LENGTH, res.content().readableBytes());

            if (keepAlive) {
                if (!req.protocolVersion().isKeepAliveDefault())
                    res.headers().set(CONNECTION, KEEP_ALIVE);
            } else {
                // Tell the client we're going to close the connection.
                res.headers().set(CONNECTION, CLOSE);
            }

            ChannelFuture f = ctx.write(res);

            if (!keepAlive)
                f.addListener(ChannelFutureListener.CLOSE);
        }

    }
}
