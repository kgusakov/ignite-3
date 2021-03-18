package org.apache.ignite.rest.netty;

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
import org.apache.ignite.rest.routes.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

public class RestApiHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Dispatcher. */
    private final Router router;

    /**
     * @param router Dispatcher.
     */
    public RestApiHandler(Router router) {
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
            FullHttpResponse res;

            var maybeRoute = router.route(req);
            if (maybeRoute.isPresent()) {
                var resp = new RestApiHttpResponse(new DefaultHttpResponse(HttpVersion.HTTP_1_1, OK));
                maybeRoute.get().handle(req, resp);
                var content = resp.content() != null ?
                    Unpooled.wrappedBuffer(resp.content()) :
                    new EmptyByteBuf(UnpooledByteBufAllocator.DEFAULT);
                res = new DefaultFullHttpResponse(resp.protocolVersion(), resp.status(),
                     content, resp.headers(), EmptyHttpHeaders.INSTANCE);
            }
            else
                res = new DefaultFullHttpResponse(req.protocolVersion(), HttpResponseStatus.NOT_FOUND);

            res.headers()
                .setInt(CONTENT_LENGTH, res.content().readableBytes());

            boolean keepAlive = HttpUtil.isKeepAlive(req);
            if (keepAlive) {
                if (!req.protocolVersion().isKeepAliveDefault())
                    res.headers().set(CONNECTION, KEEP_ALIVE);
            } else
                res.headers().set(CONNECTION, CLOSE);

            ChannelFuture f = ctx.write(res);

            if (!keepAlive)
                f.addListener(ChannelFutureListener.CLOSE);
        }

    }

    @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Failed to process http request:", cause);
        var res = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        ctx.write(res).addListener(ChannelFutureListener.CLOSE);
    }
}
