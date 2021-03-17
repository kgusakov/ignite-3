package org.apache.ignite.rest2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaselineService {
    private Set<String> consistentIds = new HashSet<>();

    public void set(List<String> consistentIds) {
        this.consistentIds.clear();
        this.consistentIds.addAll(consistentIds);
    }

    public Set<String> get() {
        return consistentIds;
    }

    public void add(List<String> consistentIds) {
        this.consistentIds.addAll(consistentIds);
    }

    public void remove(List<String> consistentIds) {
        this.consistentIds.removeAll(consistentIds);
    }
}
