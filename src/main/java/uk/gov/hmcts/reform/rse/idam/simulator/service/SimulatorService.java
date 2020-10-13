package uk.gov.hmcts.reform.rse.idam.simulator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.PinDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.SimObject;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JwTokenGenerator;

import java.util.Optional;
import java.util.Random;

@SuppressWarnings({"PMD.TooManyMethods"})
@Component
public class SimulatorService {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorService.class);
    public static final String BEARER_ = "Bearer ";
    public static final int PIN_LENGTH = 8;
    public static final int AUTH_CODE_LENGTH = 105;

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
        Optional<SimObject> userInMemory = checkUserInMemoryNotEmptyByUserName(username);
        userInMemory.get().setMostRecentBearerToken(BEARER_ + token);
    }

    public Optional<SimObject> checkUserInMemoryNotEmptyByUserName(String username) {
        Optional<SimObject> userInMemory = liveMemoryService.getByEmail(username);
        if (userInMemory.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Idam Simulator: User " + username + " not exiting"
            );
        }
        return userInMemory;
    }

    public Optional<SimObject> checkUserInMemoryNotEmptyByPin(String pin) {
        Optional<SimObject> userInMemory = liveMemoryService.getByPin(pin);
        if (userInMemory.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Idam Simulator: User with pin " + pin + " not found"
            );
        }
        return userInMemory;
    }

    @Deprecated
    public String generateOauth2CodeFromUserName(String username) {
        Optional<SimObject> userInMemory = checkUserInMemoryNotEmptyByUserName(username);
        return generateNewCode(userInMemory);
    }

    public String generateOauth2CodeFromPin(String pin) {
        Optional<SimObject> userInMemory = checkUserInMemoryNotEmptyByPin(pin);
        return generateNewCode(userInMemory);
    }

    public PinDetails createPinDetails(String authorization) {
        SimObject user = liveMemoryService.getByBearerToken(authorization).get();
        String newPinCode = generateRandomString(PIN_LENGTH);
        user.setLastGeneratedPin(newPinCode);
        PinDetails pin = new PinDetails();
        pin.setPin(newPinCode);
        pin.setUserId(user.getId());
        LOG.info("Simulator Pin Code generated {}", newPinCode);
        return pin;
    }

    public void checkUserHasBeenAuthenticateByBearerToken(String authorization) {
        if (!authorization.startsWith(BEARER_)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Idam Simulator: Bearer must start by Bearer");
        }
        if (liveMemoryService.getByBearerToken(authorization).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Idam Simulator: User not authenticated");
        }
    }

    private String generateNewCode(Optional<SimObject> userInMemory) {
        String newCode = generateRandomAlphanumeric(AUTH_CODE_LENGTH);
        userInMemory.get().setMostRecentCode(newCode);
        LOG.info("Oauth2 new code generated {}", newCode);
        return newCode;
    }

    private String generateRandomString(int targetStringLength) {
        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();
        return random.ints(leftLimit, rightLimit + 1)
            .limit(targetStringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    private String generateRandomAlphanumeric(int targetStringLength) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
            .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
            .limit(targetStringLength)
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

}
