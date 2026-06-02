package com.example.githubproxy;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableWireMock({
        @ConfigureWireMock(name = "github-api", baseUrlProperties = "github.url")
})
class GithubControllerIntegrationTest {

    @Value("${local.server.port}")
    int port;

    @InjectWireMock("github-api")
    WireMockServer wireMock;

    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(org.springframework.http.client.ClientHttpResponse response) {
                return false;
            }
        });
    }

    @Test
    void shouldReturnNonForkRepositoriesWithBranches() {
        wireMock.stubFor(WireMock.get(urlEqualTo("/users/octocat/repos"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "Hello-World",
                                    "fork": false,
                                    "owner": { "login": "octocat" }
                                  },
                                  {
                                    "name": "forked-repo",
                                    "fork": true,
                                    "owner": { "login": "octocat" }
                                  }
                                ]
                                """)));

        wireMock.stubFor(WireMock.get(urlEqualTo("/repos/octocat/Hello-World/branches"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "main",
                                    "commit": { "sha": "abc123" }
                                  },
                                  {
                                    "name": "dev",
                                    "commit": { "sha": "def456" }
                                  }
                                ]
                                """)));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/users/octocat/repositories", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .contains("Hello-World")
                .contains("octocat")
                .contains("main")
                .contains("abc123")
                .contains("dev")
                .contains("def456")
                .doesNotContain("forked-repo");
    }

    @Test
    void shouldReturnEmptyListWhenAllRepositoriesAreForks() {
        wireMock.stubFor(WireMock.get(urlEqualTo("/users/forker/repos"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                [
                                  {
                                    "name": "forked-repo",
                                    "fork": true,
                                    "owner": { "login": "forker" }
                                  }
                                ]
                                """)));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/users/forker/repositories", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Test
    void shouldReturn404WithErrorBodyForNonExistingUser() {
        wireMock.stubFor(WireMock.get(urlEqualTo("/users/ghost-user/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "message": "Not Found" }
                                """)));

        ResponseEntity<String> response = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/users/ghost-user/repositories", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody())
                .contains("\"status\":404")
                .contains("\"message\"");
    }
}