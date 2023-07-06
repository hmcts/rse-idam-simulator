package uk.gov.hmcts.reform.rse.idam.simulator.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

import static org.springframework.test.util.AssertionErrors.assertEquals;
import static org.springframework.test.util.AssertionErrors.assertTrue;

@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource("classpath:application.yaml")
@EnableAutoConfiguration
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

    @Test
    public void testLoginEndpoint() throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        URI uri = new URI("http://localhost:" + localServerPort + "/login?redirect_uri=toto&client_id=oneClientId&state=12345&ui_local=en");
        ResponseEntity<String> result = restTemplate.getForEntity(uri, String.class);
        assertEquals("Request is not successful", 200, result.getStatusCodeValue());
    }

}
