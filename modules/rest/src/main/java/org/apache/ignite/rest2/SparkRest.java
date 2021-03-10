package org.apache.ignite.rest2;

import java.util.HashSet;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import spark.Request;
import spark.Response;
import spark.Service;

import static spark.Spark.*;

public class SparkRest {

    private static Gson gson = new Gson();

    private static BaselineService baselineService = new BaselineService();

    public static void main(String[] args) {
        var spark = Service.ignite();
        get("/hello", (req, res) -> "Hello World");
        path("/v1", () -> {
            path("/baseline", () -> {
                get("/", SparkRest::baselineGet);
            });
        });
//        routes();
    }

    private static void routes() {
        path("/v1", () -> {
            path("/baseline", () -> {
                get("/", SparkRest::baselineGet);
                put("", "application/json", SparkRest::baselineSet);
                put("/add-nodes", "application/json", SparkRest::baselineAdd);
                put("/remove-nodes", "application/json", SparkRest::baselineRemove);
            });
        });
    }

    public static Object baselineSet(Request req, Response resp) {
        baselineService.set(gson.fromJson(req.body(), new TypeToken<HashSet<String>>(){}.getType()));
        return null;
    }

    public static Object baselineGet(Request req, Response resp) {
        resp.status(200);
       return baselineService.get();
    }

    public static Object baselineAdd(Request req, Response resp) {
       baselineService.add(gson.fromJson(req.body(), new TypeToken<HashSet<String>>(){}.getType()));
       return null;
    }

    public static Object baselineRemove(Request req, Response resp) {
        baselineService.remove(gson.fromJson(req.body(), new TypeToken<HashSet<String>>(){}.getType()));
        return null;
    }
}
