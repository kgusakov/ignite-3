package org.apache.ignite.rest2;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.ignite.rest.models.ConsistentIds;
import org.apache.ignite.rest2.netty.Dispatcher;
import org.apache.ignite.rest2.netty.MainNettyHandler;
import org.apache.ignite.rest2.netty.Route;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.apache.ignite.rest2.netty.Route.*;

public class NettyRest {

    private static Integer PORT = 8080;

    public static void main(String[] args) throws InterruptedException {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        var dispatcher = new Dispatcher(routes());
        var handler = new MainNettyHandler(dispatcher);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(handler);

            Channel ch = b.bind(PORT).sync().channel();

            System.err.println("Open your web browser and navigate to " +
                "http://127.0.0.1:" + PORT + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static List<Route> routes() {
        var baseline = new BaselineService();
        var objectMapper = new ObjectMapper();
        return Arrays.asList(
            get("/baseline", HttpHeaderValues.APPLICATION_JSON, (req, resp) -> baseline.get()),
            put("/baseline", HttpHeaderValues.APPLICATION_JSON, (req, resp) -> {
                ConsistentIds consistentIds = null;
                try {
                    consistentIds = objectMapper.readValue(
                        req.req.content().readCharSequence(req.req.content().readableBytes(), StandardCharsets.UTF_8).toString(), ConsistentIds.class);
                }
                catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                baseline.set(new ArrayList<>(consistentIds.consistentIds));
                resp.setStatus(OK);
                return null;
            }),
            put("/baseline/add-nodes", HttpHeaderValues.APPLICATION_JSON, (req, resp) -> {
                ConsistentIds consistentIds = null;
                try {
                    consistentIds = objectMapper.readValue(
                        req.req.content().readCharSequence(req.req.content().readableBytes(), StandardCharsets.UTF_8).toString(), ConsistentIds.class);
                }
                catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                baseline.add(new ArrayList<>(consistentIds.consistentIds));
                resp.setStatus(OK);
                return null;
            }),
            put("/baseline/remove-nodes", HttpHeaderValues.APPLICATION_JSON, (req, resp) -> {
                ConsistentIds consistentIds = null;
                try {
                    consistentIds = objectMapper.readValue(
                        req.req.content().readCharSequence(req.req.content().readableBytes(), StandardCharsets.UTF_8).toString(), ConsistentIds.class);
                }
                catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                baseline.remove(new ArrayList<>(consistentIds.consistentIds));
                resp.setStatus(OK);
                return null;

            }));
    }

}
