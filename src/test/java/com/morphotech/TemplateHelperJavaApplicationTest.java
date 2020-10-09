package com.morphotech;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.morphotech.exception.TemplatingServiceException;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TemplateHelperJavaApplicationTest {

    private HttpClient httpClient =
            mock(HttpClient.class);

    @Test
    void getTemplateExample_TEXT_HTML() throws IOException, InterruptedException {

        var json = jsonBuilder(Map.of(
                "access_token", "abcvdef",
                "expires_in", "300"));
        var httpResponseToken = httpResponseBuilder(200, json);

        byte[] b = byteArrayBuilder(20);
        var httpResponseByteArray = httpResponseBuilder(200, b);

        when(httpClient.send(any(), any()))
                .thenReturn(httpResponseToken, httpResponseByteArray);

        var templatingService = new TemplatingService(httpClient,
                "http://localhost.com",
                "http://localhost.com/auth/realms/micro-keycloak/protocol/openid-connect/token",
                "template-client-id",
                "this-is-secret");

        var response = templatingService.getTemplateExample("template-id", MediaType.TEXT_HTML);

        assertThat(response, notNullValue());
        assertThat(response.length, is(20));
    }

    @Test
    void getAllTemplates() throws IOException, InterruptedException {
        var json = jsonBuilder(Map.of(
                "access_token", "abcvdef",
                "expires_in", "300"));
        var httpResponseToken = httpResponseBuilder(200, json);

        json = jsonBuilder(Map.of(
                "template_schema", "value",
                "field1", "value1",
                "field2", "value2"));

        var httpResponseString = httpResponseBuilder(200, json);

        when(httpClient.send(any(), any()))
                .thenReturn(httpResponseToken, httpResponseString);

        var templatingService = new TemplatingService(httpClient,
                "http://localhost.com",
                "http://localhost.com/auth/realms/micro-keycloak/protocol/openid-connect/token",
                "template-client-id",
                "this-is-secret");

        var response = templatingService.getAllTemplates();
        assertThat(response.contains("template_schema"), is(true));

    }

    @Test
    void composeTemplatingFile() throws IOException, InterruptedException {
        var json = jsonBuilder(Map.of(
                "access_token", "abcvdef",
                "expires_in", "300"));
        var httpResponseToken = httpResponseBuilder(200, json);

        byte[] b = byteArrayBuilder(30);
        var httpResponseByteArray = httpResponseBuilder(200, b);

        when(httpClient.send(any(), any()))
                .thenReturn(httpResponseToken, httpResponseByteArray);

        var templatingService = new TemplatingService(httpClient,
                "http://localhost.com",
                "http://localhost.com/auth/realms/micro-keycloak/protocol/openid-connect/token",
                "template-client-id",
                "this-is-secret");


        var jsonToPost = jsonBuilder(Map.of(
                "recipient_name", "Subject 37",
                "certificate_number", "C12345"));

        var response = templatingService.composeTemplateById(
                "template-id", MediaType.APPLICATION_PDF, jsonToPost
        );

        assertThat(response, notNullValue());
        assertThat(response.length, is(30));
    }

    @Test
    void getAccessTokenNull_throw_exception() throws IOException, InterruptedException {

        when(httpClient.send(any(), any()))
                .thenReturn(null);

        var exceptionThrown = assertThrows(TemplatingServiceException.class, () -> {
            new TemplatingService(httpClient,
                    "http://localhost.com",
                    "http://localhost.com/auth/realms/micro-keycloak/protocol/openid-connect/token",
                    "template-client-id",
                    "this-is-secret");
        });

        assertThat(exceptionThrown.getMessage(), is("Failed to get access token for template service"));
    }

    @Test
    void getTemplateExample_TEXT_HTML_getNewAccessToken() throws IOException, InterruptedException {
        // Given
        var json = jsonBuilder(Map.of(
                "access_token", "abcvdef",
                "expires_in", "300"));
        var httpResponseToken = httpResponseBuilder(200, json);

        var httpResponse_401 = httpResponseBuilder(401, null);

        var json_new_token = jsonBuilder(Map.of(
                "access_token", "poiuygdsd",
                "expires_in", "300"));
        var httpResponseToken_new_token = httpResponseBuilder(200, json_new_token);

        byte[] b = byteArrayBuilder(20);
        var httpResponseByteArray = httpResponseBuilder(200, b);



        // When
        when(httpClient.send(any(), any()))
                .thenReturn(httpResponseToken, httpResponse_401, httpResponseToken_new_token, httpResponseByteArray);

        // Do request
        var templatingService = new TemplatingService(httpClient,
                "http://localhost.com",
                "http://localhost.com/auth/realms/micro-keycloak/protocol/openid-connect/token",
                "template-client-id",
                "this-is-secret");

        var response = templatingService.getTemplateExample("template-id", MediaType.TEXT_HTML);

        assertThat(response, notNullValue());
        assertThat(response.length, is(20));
    }

    // helpers

    private byte[] byteArrayBuilder(int size) {
        var b = new byte[size];
        new Random().nextBytes(b);
        return b;
    }

    private String jsonBuilder(Map<String, String> map) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(map);
    }

    private HttpResponse httpResponseBuilder(int status, Object body) {

        return new HttpResponse() {
            @Override
            public int statusCode() {
                return status;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public Object body() {
                return body;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
    }

}
