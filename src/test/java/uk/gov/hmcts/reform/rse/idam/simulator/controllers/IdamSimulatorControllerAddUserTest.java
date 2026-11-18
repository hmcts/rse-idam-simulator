package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.IdamTestingUser;
import uk.gov.hmcts.reform.rse.idam.simulator.controllers.domain.RoleDetails;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.user.SimObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage"})
public class IdamSimulatorControllerAddUserTest {

    private static final String EMAIL = "user@example.com";
    private static final String USER_ID = UUID.nameUUIDFromBytes(EMAIL.getBytes(StandardCharsets.UTF_8)).toString();

    @Test
    public void shouldNotLoseAnAccountUpdateMadeWhileATokenIsBeingStored() throws Exception {
        PausingTokenWrites userService = new PausingTokenWrites();
        userService.putSimObject(USER_ID, SimObject.builder()
            .id(USER_ID)
            .email(EMAIL)
            .roles(new ArrayList<>(List.of("old-role")))
            .build());

        SimulatorService simulatorService = new SimulatorService();
        ReflectionTestUtils.setField(simulatorService, "userService", userService);
        IdamSimulatorController controller = new IdamSimulatorController();
        ReflectionTestUtils.setField(controller, "simulatorService", simulatorService);
        ReflectionTestUtils.setField(controller, "userService", userService);

        CompletableFuture<Void> tokenUpdate =
            CompletableFuture.runAsync(() -> simulatorService.updateTokenInUser(EMAIL, "a-token"));
        assertThat(userService.tokenWriteStarted.await(10, TimeUnit.SECONDS)).isTrue();

        CompletableFuture<Void> accountUpdate = CompletableFuture.runAsync(() -> {
            controller.addNewUser(accountWithRole("new-role"));
            userService.accountWritten.countDown();
        });

        tokenUpdate.get(10, TimeUnit.SECONDS);
        accountUpdate.get(10, TimeUnit.SECONDS);

        assertThat(userService.getByUserId(USER_ID).getRoles()).containsExactly("new-role");
    }

    private IdamTestingUser accountWithRole(String role) {
        IdamTestingUser account = new IdamTestingUser();
        account.setEmail(EMAIL);
        account.setRoles(List.of(RoleDetails.build(role)));
        return account;
    }

    private static final class PausingTokenWrites extends LiveMemoryService {

        private final CountDownLatch tokenWriteStarted = new CountDownLatch(1);
        private final CountDownLatch accountWritten = new CountDownLatch(1);

        @Override
        public void putSimObject(String userId, SimObject simObject) {
            if (simObject.getMostRecentJwToken() != null) {
                tokenWriteStarted.countDown();
                awaitAccountWrite();
            }
            super.putSimObject(userId, simObject);
        }

        private void awaitAccountWrite() {
            try {
                accountWritten.await(1, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(exception);
            }
        }
    }
}
