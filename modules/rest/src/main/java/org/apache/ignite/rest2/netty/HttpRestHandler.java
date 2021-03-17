package org.apache.ignite.rest2.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpUtil;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;

public class HttpRestHandler extends SimpleChannelInboundHandler<HttpObject> {

    private final Dispatcher dispatcher;

    public HttpRestHandler(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest req = (FullHttpRequest) msg;

            boolean keepAlive = HttpUtil.isKeepAlive(req);
            var res = dispatcher.dispatch(req);
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
