package org.apache.ignite.rest2.netty;

import java.util.Collections;
import java.util.Map;
import io.netty.handler.codec.http.FullHttpRequest;

public class HttpRequestWithParams {
    private final FullHttpRequest req;

    private final Map<String, String> qryParams;

    public HttpRequestWithParams(FullHttpRequest req, Map<String, String> qryParams) {
        this.req = req;
        this.qryParams = Collections.unmodifiableMap(qryParams);
    }

    public FullHttpRequest req() {
        return req;
    }

    public Map<String, String> queryParams() {
       return qryParams;
    }
}
