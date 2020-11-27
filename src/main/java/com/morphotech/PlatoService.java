package com.morphotech;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.morphotech.exception.TemplatingServiceException;
import com.morphotech.exception.WebServiceException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class PlatoService {

    private final String baseUrl;
    private final String tokenUrl;
    private final String clientId;
    private final String secret;

    private static final String ACCEPT_HEADER = "Accept";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String BEARER_HEADER = "Bearer ";
    private static final Integer DEFAULT_REQUEST_TIMEOUT = 15;

    private HttpClient httpClient;
    private String tokenBearer;
    private int requestTimeOut = DEFAULT_REQUEST_TIMEOUT;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * For Testing purposes
     */
    public PlatoService(HttpClient httpClient, String baseUrl, String tokenUrl, String clientId, String secret) {
        this.baseUrl = baseUrl;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.secret = secret;

        this.httpClient = httpClient;
        getAccessToken();
    }

    public PlatoService(String baseUrl, String tokenUrl, String clientId, String secret) {
        this.baseUrl = baseUrl;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.secret = secret;
    }

    public PlatoService(String baseUrl, String tokenUrl, String clientId, String secret, int requestTimeout) {
        this.baseUrl = baseUrl;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.secret = secret;
        this.requestTimeOut = requestTimeout;
    }

    private void setupHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(requestTimeOut))
                    .build();
            getAccessToken();
        }
    }

    // API

    /**
     * Gets example file based on the template
     *
     * @param templateId [example: vizidox-student]
     * @param mediaType  a type of {@link MediaType}
     * @return example of file based on template
     */
    public byte[] getTemplateExample(String templateId, MediaType mediaType) throws WebServiceException {
        setupHttpClient();

        var request = buildHttpRequestHeader(
                baseUrl + "/template/" + templateId + "/example",
                Map.of(ACCEPT_HEADER, mediaType.getString(),
                        AUTHORIZATION_HEADER, BEARER_HEADER + tokenBearer),
                RequestMethod.GET
        );

        return makeRequest(request, HttpResponse.BodyHandlers.ofByteArray()).body();
    }

    /**
     * @return template information
     */
    public String getAllTemplates() throws WebServiceException {
        setupHttpClient();

        var request = buildHttpRequestHeader(
                baseUrl + "/templates/",
                Map.of(AUTHORIZATION_HEADER, BEARER_HEADER + tokenBearer),
                RequestMethod.GET
        );

        return makeRequest(request, HttpResponse.BodyHandlers.ofString()).body();
    }

    /**
     * Composes file based on the template
     *
     * @param templateId id of the template on service
     * @param mediaType  determines what kind of file is outputed. One of {@link MediaType}
     * @param schema     body to compose file with, must be according to the template schema
     * @return composed file
     */
    public byte[] composeTemplateById(String templateId, MediaType mediaType, String schema) throws WebServiceException {
        setupHttpClient();

        var request = buildHttpRequestHeader(
                baseUrl + "/template/" + templateId + "/compose",
                Map.of(ACCEPT_HEADER, mediaType.getString(),
                        AUTHORIZATION_HEADER, BEARER_HEADER + tokenBearer,
                        CONTENT_TYPE_HEADER, "application/json"),
                RequestMethod.POST,
                schema
        );

        return makeRequest(request, HttpResponse.BodyHandlers.ofByteArray()).body();
    }


    // Authentication

    /**
     * Makes request to get Access Token and sets it on local variable
     */
    private void getAccessToken() {

        String tokenRequest =
                "client_id=" + clientId
                        + "&client_secret=" + secret
                        + "&grant_type=" + "client_credentials"
                        + "&scope=" + "content-provider-scope";

        HttpRequest oAuth2Request = buildHttpRequestHeader(
                tokenUrl,
                Map.of(
                        ACCEPT_HEADER, "application/json",
                        CONTENT_TYPE_HEADER, "application/x-www-form-urlencoded"),
                RequestMethod.POST,
                tokenRequest
        );

        try {
            var response = httpClient.send(oAuth2Request, HttpResponse.BodyHandlers.ofString());

            if (null != response && null != response.body()) {
                Map<String, Object> map
                        = objectMapper.readValue(response.body(), new TypeReference<>() {
                });

                tokenBearer = (String) map.get("access_token");
            } else {
                throw new TemplatingServiceException("Failed to get access token for template service");
            }
        } catch (IOException e) {
            throw new TemplatingServiceException("Error while trying to get auth token for template service", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TemplatingServiceException("Error while trying to get auth token for template service", e);
        }
    }

    /**
     * HTTP send request method
     * <p>
     * This method exists so that the application can resend the request with a new access token in case of 401
     *
     * @param request
     * @param bodyHandler
     * @param <T>
     * @return
     */
    private <T> HttpResponse<T> makeRequest(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) throws WebServiceException {
        try {

            var response = httpClient.send(request, bodyHandler);

            if (response.statusCode() != 200) {
                getAccessToken();
                response = httpClient.send(request, bodyHandler);

                if (response.statusCode() != 200) {
                    throw new WebServiceException("Failed to access Plato Service");
                }
            }

            return response;

        } catch (IOException e) {
            throw new TemplatingServiceException("Failed to send or receive request through HTTP client");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TemplatingServiceException("The thread was interrupted. Failed to access Plato Service");
        }
    }

    // HTTP Header builders

    /**
     * Override of {@link #buildHttpRequestHeader(String, Map, RequestMethod, String)}
     *
     * @param url
     * @param headerMap
     * @param requestMethod
     * @return
     */
    private HttpRequest buildHttpRequestHeader(String url, Map<String, String> headerMap, RequestMethod
            requestMethod) {
        return buildHttpRequestHeader(url, headerMap, requestMethod, null);
    }

    /**
     * This method exists to make it easier to build the headers of the HTTP request
     * It also validates the requestMethod
     *
     * @param url
     * @param headerMap
     * @param requestMethod
     * @param bodyContent
     * @return
     */
    private HttpRequest buildHttpRequestHeader(String url, Map<String, String> headerMap, RequestMethod
            requestMethod, String bodyContent) {
        var httpRequest = HttpRequest.newBuilder(URI.create(url));

        if (null != headerMap && !headerMap.isEmpty()) {
            headerMap.keySet().forEach(key -> httpRequest.setHeader(key, headerMap.get(key)));
        }

        switch (requestMethod) {
            case GET:
                return httpRequest.GET().build();
            case POST:
                return httpRequest.POST(HttpRequest.BodyPublishers.ofString(bodyContent)).build();
            default:
                throw new TemplatingServiceException("HTTP Request method not recognized: " + requestMethod);
        }
    }

}
