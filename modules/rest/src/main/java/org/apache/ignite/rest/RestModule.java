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
import io.javalin.plugin.openapi.OpenApiOptions;
import io.javalin.plugin.openapi.OpenApiPlugin;
import io.javalin.plugin.openapi.dsl.OpenApiBuilder;
import io.javalin.plugin.openapi.ui.SwaggerOptions;
import io.swagger.v3.oas.models.info.Info;
import org.apache.ignite.configuration.ConfigurationRegistry;
import org.apache.ignite.configuration.Configurator;
import org.apache.ignite.rest.configuration.InitRest;
import org.apache.ignite.rest.configuration.RestConfigurationImpl;
import org.apache.ignite.rest.configuration.Selectors;
import org.apache.ignite.rest.handlers.BaselineHandlers;
import org.apache.ignite.rest.handlers.ConfigurationHandlers;
import org.apache.ignite.rest.models.ConsistentIds;
import org.apache.ignite.rest.presentation.ConfigurationPresentation;
import org.apache.ignite.rest.presentation.FormatConverter;
import org.apache.ignite.rest.presentation.json.JsonConverter;
import org.apache.ignite.rest.presentation.json.JsonPresentation;
import org.slf4j.Logger;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.put;

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
        try {
            Class.forName(Selectors.class.getName());
        }
        catch (ClassNotFoundException e) {
            // No-op.
        }

        sysConf = sysConfig;

        presentation = new JsonPresentation(sysConfig.getConfigurators());

        FormatConverter converter = new JsonConverter();

        Configurator<RestConfigurationImpl> restConf = Configurator.create(RestConfigurationImpl::new,
            converter.convertFrom(moduleConfReader, "rest", InitRest.class));

        sysConfig.registerConfigurator(restConf);
    }

    /** */
    public void start() {
        Javalin app = startRestEndpoint();
//        routes(app);
    }

    private void routes(Javalin app) {
        var configurationHandlers = new ConfigurationHandlers(presentation);
        var baselineHandlers = new BaselineHandlers();

        app.routes(() -> {
            path("v1/configuration", () -> {
                get(configurationHandlers::get);
                get("/:selector", configurationHandlers::getByPath);
                post(configurationHandlers::set);
            });

            path("v1/baseline", () -> {
                get("",
                    OpenApiBuilder.documented(
                        OpenApiBuilder
                            .document()
                            .json("200", ConsistentIds.class),
                        baselineHandlers::get));

                put("",
                    OpenApiBuilder.documented(
                        OpenApiBuilder
                            .document()
                            .result("200").body(ConsistentIds.class),
                        baselineHandlers::set));

                put("/add-nodes",
                    OpenApiBuilder.documented(
                        OpenApiBuilder
                            .document()
                            .body(ConsistentIds.class)
                            .result("200"),
                        baselineHandlers::add));

                put("remove-nodes",
                    OpenApiBuilder.documented(
                        OpenApiBuilder.document()
                            .body(ConsistentIds.class).result("200"),
                        baselineHandlers::remove));
            });
        });
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


        app = Javalin.create(
            config -> config.registerPlugin(
                new OpenApiPlugin(
                    new OpenApiOptions(
                        new Info().description("Apache Ignite REST API")
                    )
                        .path("/swagger-docs")
                        .swagger(new SwaggerOptions("/swagger").title("My Swagger Documentation"))
                        .activateAnnotationScanningFor("org.apache.ignite.rest")
                )
            )
        );
        routes(app);
        app.start(_port);
        log.info("REST protocol started successfully on port " + app.port());

        return app;
    }

    /** */
    public String configRootKey() {
        return "rest";
    }
}
