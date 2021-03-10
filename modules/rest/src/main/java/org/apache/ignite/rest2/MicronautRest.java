package org.apache.ignite.rest2;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.runtime.Micronaut;
import io.reactivex.Single;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.apache.ignite.rest.handlers.BaselineHandlers;
import org.apache.ignite.rest.models.ConsistentIds;

@OpenAPIDefinition(
    info = @Info(
        title = "Hello World",
        version = "0.0",
        description = "My API",
        license = @License(name = "Apache 2.0", url = "https://foo.bar"),
        contact = @Contact(url = "https://apache-ignite", name = "Fred", email = "fred@apache-ignite")
    )
)
public class MicronautRest {

    public static void main(String[] args) {
        Micronaut.run(MicronautRest.class);
    }

    @Controller("/v1/baseline")
    public static class BaselineController {

        private BaselineService baselineService = new BaselineService();

        @Get(value = "/", produces = MediaType.APPLICATION_JSON)
        @ApiResponse(responseCode = "200", description = "Consistent ids of baseline")
        public ConsistentIds get() {
           return new ConsistentIds(baselineService.get());
        }

    }

}
