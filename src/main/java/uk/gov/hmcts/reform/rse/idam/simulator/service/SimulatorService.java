package uk.gov.hmcts.reform.rse.idam.simulator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.PinDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JwTokenGeneratorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.SimObject;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.UserService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.LawOfDemeter"})
@Component
public class SimulatorService {

    private static final Logger LOG = LoggerFactory.getLogger(SimulatorService.class);
    public static final int PIN_LENGTH = 8;
    public static final int AUTH_CODE_LENGTH = 27;

    @Autowired
    private JwTokenGeneratorService jwTokenGenerator;

    @Autowired
    private UserService userService;

    @Value("${simulator.jwt.issuer}")
    private String issuer;

    @Value("${simulator.jwt.expiration}")
    private long tokenExpirationMs;

    public String generateAuthTokenFromCode(String code, String serviceId, String grantType) {
        Optional<SimObject> userInMemory = userService.getByCode(code);
        String token = generateAToken(userInMemory.get().getEmail(), serviceId, grantType);
        LOG.info("Oauth2 Token Generated {} for {}", token, userInMemory.get().getEmail());
        if (userInMemory.isEmpty()) {
            LOG.warn("No User for this code " + code);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Idam Simulator: No User for this code " + code);
        }
        return token;
    }

    public String generateAToken(String userName, String clientID, String grantType) {
        return jwTokenGenerator.generateToken(issuer, tokenExpirationMs, userName, clientID, grantType);
    }

    public String generateACachedToken(String userName, String clientID, String grantType) {
        Optional<SimObject> userInMemory = userService.getByEmail(userName);
        if (userInMemory.isEmpty()) {
            LOG.warn("No User for this userName " + userName
                         + ". Usually this happens because the user has not been added to the system"
                         + " by a post on testing-support/accounts");
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Idam Simulator: No User for this userName " + userName
            );
        }
        Boolean tokenExpired = userService.getByEmail(userName).stream()
            .map(SimObject::getMostRecentJwTokenUnixTime)
            .map(t -> System.currentTimeMillis() > t + tokenExpirationMs).findFirst().get();

        if (tokenExpired || userService.getByEmail(userName).get().getMostRecentJwToken() == null) {
            LOG.info("Token not existing or expired and will be regenerated");
            return jwTokenGenerator.generateToken(issuer, tokenExpirationMs, userName, clientID, grantType);
        }
        LOG.info("Use token in cache");
        return userService.getByEmail(userName).get().getMostRecentJwToken();
    }

    public void updateTokenInUser(String username, String token) {
        Optional<SimObject> userInMemory = checkUserInMemoryNotEmptyByUserName(username);
        SimObject user = userInMemory.get();
        user.setMostRecentJwToken(token);
        userService.putSimObject(user.getId(), user);
    }

    public void updateTokenInUserFromCode(String code, String token) {
        Optional<SimObject> userInMemory = userService.getByCode(code);
        SimObject user = userInMemory.get();
        user.setMostRecentJwToken(token);
        userService.putSimObject(user.getId(), user);
    }

    public Optional<SimObject> checkUserInMemoryNotEmptyByUserName(String username) {
        Optional<SimObject> userInMemory = userService.getByEmail(username);
        if (userInMemory.isEmpty()) {
            LOG.warn("User " + username + " not exiting");
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Idam Simulator: User " + username + " not exiting"
            );
        }
        return userInMemory;
    }

    public Optional<SimObject> checkUserInMemoryNotEmptyByPin(String pin) {
        Optional<SimObject> userInMemory = userService.getByPin(pin);
        if (userInMemory.isEmpty()) {
            LOG.warn("User with pin " + pin + " not found");
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

    public PinDetails createPinDetails(String firstName, String lastName) {
        SimObject user = userService.getByName(firstName, lastName).get();
        String newPinCode = generateRandomString(PIN_LENGTH);
        user.setLastGeneratedPin(newPinCode);
        userService.putSimObject(user.getId(), user);
        PinDetails pin = new PinDetails();
        pin.setPin(newPinCode);
        pin.setUserId(user.getId());
        final String expiry = String.valueOf(java.sql.Timestamp.valueOf(LocalDateTime.now().minusHours(4)).getTime());
        pin.setExpiry(expiry);
        LOG.info("Simulator Pin Code generated {}", newPinCode);
        return pin;
    }

    public void checkUserHasBeenAuthenticateByBearerToken(String authorization) {
        if (!authorization.startsWith(SimObject.BEARER_)) {
            LOG.warn("Bearer must start by Bearer");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Idam Simulator: Bearer must start by Bearer");
        }
        if (userService.getByJwToken(authorization).isEmpty()) {
            LOG.warn("User not authenticated");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Idam Simulator: User not authenticated");
        }
    }

    private String generateNewCode(Optional<SimObject> userInMemory) {
        String newCode = getNewAuthCode();
        SimObject user = userInMemory.get();
        user.setMostRecentCode(newCode);
        userService.putSimObject(user.getId(), user);
        return newCode;
    }

    public String geAuthCodeFromUserName(String email) {
        Optional<SimObject> userInMemory = checkUserInMemoryNotEmptyByUserName(email);
        String mostRecentCode = userInMemory.get().getMostRecentCode();
        if (mostRecentCode == null || mostRecentCode.isEmpty()) {
            mostRecentCode = generateNewCode(userInMemory);
        }
        return mostRecentCode;
    }

    public String getNewAuthCode() {
        String newCode = generateRandomAlphanumeric(AUTH_CODE_LENGTH);
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

    public String getNewIdamSessionValue() {
        String newIdamSession = generateRandomAlphanumeric(64);
        LOG.info("New Idam Session Value generated {}", newIdamSession);
        return newIdamSession;
    }
}
