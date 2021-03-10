package org.apache.ignite.rest.models;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ConsistentIds {
    public final Set<String> consistentIds;

    @JsonCreator
    public ConsistentIds(@JsonProperty("consistentIds") Set<String> consistentIds) {
        this.consistentIds = consistentIds;
    }
}
