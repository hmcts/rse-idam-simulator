package uk.gov.hmcts.reform.rse.idam.simulator.service.user;

import com.nimbusds.jose.JWSObject;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "simulator.storage.type", havingValue = "memory")
public class LiveMemoryService implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(LiveMemoryService.class);

    protected Map<String, SimObject> memories = new ConcurrentHashMap<>();

    @Override
    public Optional<SimObject> getByEmail(String email) {
        return memories.entrySet().stream()
            .filter(es -> es.getValue().getEmail().equalsIgnoreCase(email))
            .map(es -> es.getValue())
            .findFirst();
    }

    @Override
    @SneakyThrows
    public Optional<SimObject> getByJwToken(String bearerToken) {
        var email = JWSObject.parse(bearerToken.replace(SimObject.BEARER_, ""))
            .getPayload().toJSONObject()
            .getAsString("sub");
        return memories.values().stream()
            .filter(simObject -> simObject != null && simObject.getEmail() != null)
            .filter(simObject -> simObject.getEmail()
              .equalsIgnoreCase(email)
            )
            .findFirst();
    }

    @Override
    public Optional<SimObject> getByName(String firstName, String lastName) {
        return memories.entrySet().stream()
            .filter(es -> es.getValue() != null)
            .filter(es -> es.getValue().getForename().equalsIgnoreCase(firstName)
                && es.getValue().getSurname().equalsIgnoreCase(
                lastName)
            )
            .map(es -> es.getValue())
            .findFirst();
    }

    @Override
    public Optional<SimObject> getByCode(String code) {
        return memories.entrySet().stream()
            .filter(es -> es.getValue() != null && es.getValue().getMostRecentCode() != null)
            .filter(es -> es.getValue().getMostRecentCode().equalsIgnoreCase(code))
            .map(es -> es.getValue())
            .findFirst();
    }

    @Override
    public Optional<SimObject> getByPin(String pin) {
        return memories.entrySet().stream()
            .filter(es -> es.getValue() != null && es.getValue().getLastGeneratedPin() != null)
            .filter(es -> es.getValue().getLastGeneratedPin().equalsIgnoreCase(pin))
            .map(es -> es.getValue())
            .findFirst();
    }

    @Override
    public SimObject getByUserId(String userId) {
        return memories.get(userId);
    }

    @Override
    public void putSimObject(String userId, SimObject simObject) {
        LOG.info("Add Object userId {} {}", userId, simObject);
        memories.put(userId, simObject);
        LOG.info("Number of object in memory {}", this.memories.size());
    }

}
