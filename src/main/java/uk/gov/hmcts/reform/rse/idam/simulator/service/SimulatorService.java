package uk.gov.hmcts.reform.rse.idam.simulator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.SimObject;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JwTokenGenerator;

import java.util.Optional;

@Component
public class SimulatorService {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorService.class);
    public static final String BEARER_ = "Bearer ";

    @Autowired
    private LiveMemoryService liveMemoryService;

    @Value("${simulator.jwt.issuer}")
    private String issuer;

    @Value("${simulator.jwt.expiration}")
    private long expiration;

    public String generateAuthTokenFromCode(String code) {
        String token = JwTokenGenerator.generateToken(issuer, expiration);
        LOG.info("Oauth2 Token Generated {}", token);
        Optional<SimObject> userInMemory = liveMemoryService.getByCode(code);
        if (userInMemory.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Idam Simulator: No User for this code " + code);
        }
        return token;
    }

    public String generateAToken() {
        return JwTokenGenerator.generateToken(issuer, expiration);
    }

    public void updateTokenInUser(String username, String token) {
        Optional<SimObject> userInMemory = checkUserInMemoryNotEmpty(username);
        userInMemory.get().setMostRecentBearerToken(BEARER_ + token);
    }

    public Optional<SimObject> checkUserInMemoryNotEmpty(String username) {
        Optional<SimObject> userInMemory = liveMemoryService.getByEmail(username);
        if (userInMemory.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Idam Simulator: User " + username + " not exiting"
            );
        }
        return userInMemory;
    }

    @Deprecated
    public String generateOauth2Code(String username) {
        Optional<SimObject> userInMemory = checkUserInMemoryNotEmpty(username);
        String newCode = Long.toString(System.currentTimeMillis());
        userInMemory.get().setMostRecentCode(newCode);
        LOG.info("Oauth2 new code generated {}", newCode);
        return newCode;
    }

    public  void checkUserHasBeenAuthenticateByBearerToken(String authorization) {
        if (!authorization.startsWith(BEARER_)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Idam Simulator: Bearer must start by Bearer");
        }
        if (liveMemoryService.getByBearerToken(authorization).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Idam Simulator: User not authenticated");
        }
    }
}
