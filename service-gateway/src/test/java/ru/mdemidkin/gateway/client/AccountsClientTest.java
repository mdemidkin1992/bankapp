package ru.mdemidkin.gateway.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.signup.SignupRequest;
import ru.mdemidkin.libdto.signup.SignupResponse;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class AccountsClientWireMockTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private WireMockServer wireMock;
    private AccountsClient client;

    @BeforeEach
    void setUp() throws Exception {
        wireMock = new WireMockServer(
                WireMockConfiguration.options()
                        .dynamicPort());
        wireMock.start();

        WebClient webClient = WebClient.builder().build();
        client = new AccountsClient(webClient);

        ReflectionTestUtils.setField(
                client,
                "gateway",
                "localhost:" + wireMock.port()
        );


        wireMock.stubFor(post(urlEqualTo("/api/signup"))
                .withHeader("Content-Type", containing("application/json"))
                .withRequestBody(equalToJson(objectMapper.writeValueAsString(getSignupRequest())))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json;charset=UTF-8")
                        .withBody(objectMapper.writeValueAsString(getSignupResponse()))
                )
        );
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void signup_shouldDeserializeSuccessfulResponse() {
        SignupRequest req = SignupRequest.builder()
                .login("joe")
                .name("Joe User")
                .password("pass123")
                .birthdate("1990-01-01")
                .build();

        Mono<SignupResponse> mono = client.signup(req);
        SignupResponse resp = mono.block();

        assertNotNull(resp);
        assertEquals("joe", resp.getLogin());
        assertEquals("Joe User", resp.getName());
        assertNull(resp.getErrors());
    }

    private SignupRequest getSignupRequest() {
        return SignupRequest.builder()
                .login("joe")
                .name("Joe User")
                .password("pass123")
                .birthdate("1990-01-01")
                .build();
    }

    private SignupResponse getSignupResponse() {
        return SignupResponse.builder()
                .login("joe")
                .name("Joe User")
                .errors(null)
                .build();
    }
}
