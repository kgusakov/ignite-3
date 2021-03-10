package org.apache.ignite.rest.handlers;

import java.util.HashSet;
import java.util.Set;
import io.javalin.http.Context;
import io.javalin.plugin.openapi.annotations.HttpMethod;
import io.javalin.plugin.openapi.annotations.OpenApi;
import io.javalin.plugin.openapi.annotations.OpenApiContent;
import io.javalin.plugin.openapi.annotations.OpenApiRequestBody;
import io.javalin.plugin.openapi.annotations.OpenApiResponse;
import org.apache.ignite.rest.models.ConsistentIds;

public class BaselineHandlers {
    private Set<String> consistentIds = new HashSet<>();

    @OpenApi(
        requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ConsistentIds.class)),
        responses = {
            @OpenApiResponse(status = "200")
        }
    )
    public void set(Context ctx) {
        var ids = ctx.bodyValidator(ConsistentIds.class);
        consistentIds.clear();
        consistentIds.addAll(ids.getValue().consistentIds);
    }

    @OpenApi(
        method = HttpMethod.GET,
        path = "/va1/baseline",
        responses = {
            @OpenApiResponse(status = "200", content = @OpenApiContent(from = ConsistentIds.class)),
            @OpenApiResponse(status = "404")
        }
    )
    public void get(Context ctx) {
        ctx.json(new ConsistentIds(consistentIds));
    }

    @OpenApi(
        requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ConsistentIds.class)),
        responses = {
            @OpenApiResponse(status = "200")
        }
    )
    public void add(Context ctx) {
        var ids = ctx.bodyValidator(ConsistentIds.class);
        consistentIds.addAll(ids.get().consistentIds);
    }

    @OpenApi(
        requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = ConsistentIds.class)),
        responses = {
            @OpenApiResponse(status = "200")
        }
    )
    public void remove(Context ctx) {
        consistentIds.removeAll(ctx.bodyValidator(ConsistentIds.class).get().consistentIds);
    }
}
