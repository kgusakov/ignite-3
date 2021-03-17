/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ignite.rest;

import io.javalin.Javalin;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.ignite.configuration.ConfigurationRegistry;
import org.apache.ignite.rest.configuration.RestConfigurationImpl;
import org.apache.ignite.rest.presentation.ConfigurationPresentation;
import org.apache.ignite.rest.presentation.json.JsonPresentation;
import org.apache.ignite.rest2.netty.Dispatcher;
import org.apache.ignite.rest2.netty.MainNettyHandler;
import org.apache.ignite.rest2.netty.Route;
import org.slf4j.Logger;

import static org.apache.ignite.rest2.netty.Route.*;

/**
 * Rest module is responsible for starting a REST endpoints for accessing and managing configuration.
 *
 * It is started on port 10300 by default but it is possible to change this in configuration itself.
 * Refer to default config file in resources for the example.
 */
public class RestModule {
    /** */
    private static final int DFLT_PORT = 10300;

    /** */
    private static final String CONF_URL = "/management/v1/configuration/";

    /** */
    private static final String PATH_PARAM = "selector";

    /** */
    private ConfigurationRegistry sysConf;

    /** */
    private volatile ConfigurationPresentation<String> presentation;

    /** */
    private final Logger log;

    /** */
    public RestModule(Logger log) {
        this.log = log;
    }

    /** */
    public void prepareStart(ConfigurationRegistry sysConfig, Reader moduleConfReader) {
        sysConf = sysConfig;

        presentation = new JsonPresentation(Collections.emptyMap());

//        FormatConverter converter = new JsonConverter();
//
//        Configurator<RestConfigurationImpl> restConf = Configurator.create(RestConfigurationImpl::new,
//            converter.convertFrom(moduleConfReader, "rest", InitRest.class));
//
//        sysConfig.registerConfigurator(restConf);
    }

    /** */
    public void start() throws InterruptedException {
//        Javalin app = startRestEndpoint();
//
//        FormatConverter converter = new JsonConverter();

        var routes = Arrays.asList(
            get(CONF_URL, HttpHeaderValues.APPLICATION_JSON, (req, resp) -> {
                return presentation.represent();
            }),

            get(CONF_URL + ":" + PATH_PARAM, (req, resp) -> {
               String cfgPath = req.qryParams.get(PATH_PARAM);
               return presentation.representByPath(cfgPath);
            }),

            put(CONF_URL, HttpHeaderValues.APPLICATION_JSON, (req, resp) -> {
                presentation.update(req.req.content().readCharSequence(req.req.content().readableBytes(), StandardCharsets.UTF_8).toString());
                return null;
            })
        );

        startRestEndpoint(routes);
/*
        app.get(CONF_URL, ctx -> {
            ctx.result(presentation.represent());
        });

        app.get(CONF_URL + ":" + PATH_PARAM, ctx -> {
            String configPath = ctx.pathParam(PATH_PARAM);

            try {
                ctx.result(presentation.representByPath(configPath));
            }
            catch (IllegalArgumentException pathE) {
                ErrorResult eRes = new ErrorResult("CONFIG_PATH_UNRECOGNIZED", pathE.getMessage());

                ctx.status(400).result(converter.convertTo("error", eRes));
            }
        });

        app.post(CONF_URL, ctx -> {
            try {
                presentation.update(ctx.body());
            }
            catch (IllegalArgumentException argE) {
                ErrorResult eRes = new ErrorResult("CONFIG_PATH_UNRECOGNIZED", argE.getMessage());

                ctx.status(400).result(converter.convertTo("error", eRes));
            }
            catch (ConfigurationValidationException validationE) {
                ErrorResult eRes = new ErrorResult("APPLICATION_EXCEPTION", validationE.getMessage());

                ctx.status(400).result(converter.convertTo("error", eRes));
            }
            catch (JsonSyntaxException e) {
                String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();

                ErrorResult eRes = new ErrorResult("VALIDATION_EXCEPTION", msg);

                ctx.status(400).result(converter.convertTo("error", eRes));
            }
            catch (Exception e) {
                ErrorResult eRes = new ErrorResult("VALIDATION_EXCEPTION", e.getMessage());

                ctx.status(400).result(converter.convertTo("error", eRes));
            }
        });*/
    }

    /** */
    private Javalin startRestEndpoint() {
        Integer port = sysConf.getConfiguration(RestConfigurationImpl.KEY).port().value();
        Integer portRange = sysConf.getConfiguration(RestConfigurationImpl.KEY).portRange().value();

        Javalin app = null;
        int _port = 0;

        if (portRange == null || portRange == 0) {
            try {
               _port = (port != null ? port : DFLT_PORT);
            }
            catch (RuntimeException e) {
                log.warn("Failed to start REST endpoint: ", e);

                throw e;
            }
        }
        else {
            int startPort = port;

            for (int portCandidate = startPort; portCandidate < startPort + portRange; portCandidate++) {
                try {
                    _port = (portCandidate);
                }
                catch (RuntimeException ignored) {
                    // No-op.
                }

                if (app != null)
                    break;
            }

            if (_port == 0) {
                String msg = "Cannot start REST endpoint. " +
                    "All ports in range [" + startPort + ", " + (startPort + portRange) + ") are in use.";

                log.warn(msg);

                throw new RuntimeException(msg);
            }
        }

        app = Javalin.create();
        app.start(_port);
        log.info("REST protocol started successfully on port " + app.port());

        return app;
    }

    public void startRestEndpoint(List<Route> routes) throws InterruptedException {
//        Integer port = sysConf.getConfiguration(RestConfigurationImpl.KEY).port().value();
//        Integer portRange = sysConf.getConfiguration(RestConfigurationImpl.KEY).portRange().value();
        Integer port = 8080;
        Integer portRange = 0;

        Javalin app = null;
        int _port = 0;

        if (portRange == null || portRange == 0) {
            try {
                _port = (port != null ? port : DFLT_PORT);
            }
            catch (RuntimeException e) {
                log.warn("Failed to start REST endpoint: ", e);

                throw e;
            }
        }
        else {
            int startPort = port;

            for (int portCandidate = startPort; portCandidate < startPort + portRange; portCandidate++) {
                try {
                    _port = (portCandidate);
                }
                catch (RuntimeException ignored) {
                    // No-op.
                }

                if (app != null)
                    break;
            }

            if (_port == 0) {
                String msg = "Cannot start REST endpoint. " +
                    "All ports in range [" + startPort + ", " + (startPort + portRange) + ") are in use.";

                log.warn(msg);

                throw new RuntimeException(msg);
            }
        }

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        var dispatcher = new Dispatcher(routes);
        var handler = new MainNettyHandler(dispatcher);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 1024);
            b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(handler);

            Channel ch = b.bind(_port).sync().channel();

            System.err.println("Open your web browser and navigate to " +
                "http://127.0.0.1:" + _port + '/');

            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    /** */
    public String configRootKey() {
        return "rest";
    }
}
