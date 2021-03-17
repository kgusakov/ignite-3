package org.apache.ignite.rest2.netty;

import java.util.Collections;
import java.util.Map;
import io.netty.handler.codec.http.FullHttpRequest;

public class RestApiHttpRequest {
    /** Request. */
    private final FullHttpRequest req;

    /** Query params. */
    private final Map<String, String> qryParams;

    /**
     * @param req Request.
     * @param qryParams Query params.
     */
    public RestApiHttpRequest(FullHttpRequest req, Map<String, String> qryParams) {
        this.req = req;
        this.qryParams = Collections.unmodifiableMap(qryParams);
    }

    public FullHttpRequest request() {
        return req;
    }

    public Map<String, String> queryParams() {
       return qryParams;
    }
}
