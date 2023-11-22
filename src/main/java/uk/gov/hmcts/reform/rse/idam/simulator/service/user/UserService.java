package uk.gov.hmcts.reform.rse.idam.simulator.service.user;

import java.util.Optional;

public interface UserService {

    Optional<SimObject> getByEmail(String email);

    Optional<SimObject> getByJwToken(String bearerToken);

    Optional<SimObject> getByName(String firstName, String lastName);

    Optional<SimObject> getByCode(String code);

    Optional<SimObject> getByPin(String pin);

    SimObject getByUserId(String userId);

    void putSimObject(String userId, SimObject simObject);
}
