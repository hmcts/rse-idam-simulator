package uk.gov.hmcts.reform.rse.idam.simulator.service.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LiveMemoryService {

    private static final Logger LOG = LoggerFactory.getLogger(LiveMemoryService.class);

    private final Map<String, SimObject> memories = new ConcurrentHashMap<>();

    public Optional<SimObject> getByEmail(String email) {
        return memories.entrySet().stream()
            .filter(es -> es.getValue().getEmail().equalsIgnoreCase(email))
            .map(es -> es.getValue())
            .findFirst();
    }

    public Optional<SimObject> getByBearerToken(String bearerToken) {
        return memories.entrySet().stream()
            .filter(es -> es.getValue() != null && es.getValue().getMostRecentBearerToken() != null)
            .filter(es -> es.getValue().getMostRecentBearerToken().equalsIgnoreCase(bearerToken))
            .map(es -> es.getValue())
            .findFirst();
    }

    public Optional<SimObject> getByCode(String code) {
        return memories.entrySet().stream()
            .filter(es -> es.getValue() != null && es.getValue().getMostRecentCode() != null)
            .filter(es -> es.getValue().getMostRecentCode().equalsIgnoreCase(code))
            .map(es -> es.getValue())
            .findFirst();
    }

    public Optional<SimObject> getByPin(String pin) {
        return memories.entrySet().stream()
            .filter(es -> es.getValue() != null && es.getValue().getLastGeneratedPin() != null)
            .filter(es -> es.getValue().getLastGeneratedPin().equalsIgnoreCase(pin))
            .map(es -> es.getValue())
            .findFirst();
    }

    public SimObject getByUserId(String userId) {
        return memories.get(userId);
    }

    public void putSimObject(String userId, SimObject simObject) {
        LOG.info("Add Object userId {} {}", userId, simObject);
        memories.put(userId, simObject);
        LOG.info("Number of object in memory {}", this.memories.size());
    }

}
