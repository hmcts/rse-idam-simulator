package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.idam.client.IdamApi;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.rse.idam.simulator.service.SimulatorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.memory.LiveMemoryService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JsonWebKeyService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.JwTTokenGeneratorService;
import uk.gov.hmcts.reform.rse.idam.simulator.service.token.OpenIdConfigService;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@SpringBootTest(classes = {IdamClient.class, IdamApi.class, IdamSimulatorController.class, LiveMemoryService.class,
    SimulatorService.class, JsonWebKeyService.class, JwTTokenGeneratorService.class, OpenIdConfigService.class},
     webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@PropertySource("classpath:application.yaml")
@EnableAutoConfiguration
@RunWith(SpringRunner.class)
public class GetWelcomeSpringBootTest {

    @LocalServerPort
    int localServerPort;

    @Test
    public void testRootHealthEndpoint() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        URI uri = new URI("http://localhost:" + localServerPort + "/health");
        ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
        assertEquals("Request is not successful", 200, result.getStatusCodeValue());
        assertTrue("Body doesn't contents status", result.getBody().contains("status"));
    }

}
