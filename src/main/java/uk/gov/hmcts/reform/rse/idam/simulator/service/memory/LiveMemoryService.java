package uk.gov.hmcts.reform.rse.idam.simulator.service.memory;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LiveMemoryService {

    public static final String BEARER_ = "Bearer ";
    private final Map<String, SimObject> memories = new ConcurrentHashMap<>();

    public SimObject getSimObject(String key) {
        return memories.get(key.replace(BEARER_,""));
    }

    public void putSimObject(String key, SimObject simObject) {
        memories.put(key.replace(BEARER_, ""), simObject);
    }

}
