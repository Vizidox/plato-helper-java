package com.morphotech;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.morphotech.exception.TemplatingServiceException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class TemplatingService {

    private final String templatingBaseUrl;

    private final String templatingTokenUrl;

    private final String templatingClientId;

    private final String templatingSecret;

    private HttpClient httpClient;
    private String tokenBearer;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * For Testing purposes
     */
    public TemplatingService(HttpClient httpClient, String templatingBaseUrl, String templatingTokenUrl, String templatingClientId, String templatingSecret) {
        this.templatingBaseUrl = templatingBaseUrl;
        this.templatingTokenUrl = templatingTokenUrl;
        this.templatingClientId = templatingClientId;
        this.templatingSecret = templatingSecret;

        this.httpClient = httpClient;
        getAccessToken();
    }

    public TemplatingService(String templatingBaseUrl, String templatingTokenUrl, String templatingClientId, String templatingSecret) {
        this.templatingBaseUrl = templatingBaseUrl;
        this.templatingTokenUrl = templatingTokenUrl;
        this.templatingClientId = templatingClientId;
        this.templatingSecret = templatingSecret;

        setup();
    }

    private void setup() {
        httpClient = HttpClient.newBuilder().build();
        getAccessToken();
    }

    // API

    /**
     * Gets example file based on the template
     *
     * @param templateId [example: vizidox-student]
     * @param mediaType  a type of {@link MediaType}
     * @return example of file based on template
     */
    public byte[] getTemplateExample(String templateId, MediaType mediaType) {

        var request = buildHttpRequestHeader(
                templatingBaseUrl + "/template/" + templateId + "/example",
                Map.of("Accept", mediaType.getString(),
                        "Authorization", "Bearer " + tokenBearer),
                RequestMethod.GET
        );

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
        } catch (IOException | InterruptedException e) {
            throw new TemplatingServiceException("Error while trying to access service", e);
        }
    }

    /**
     * @return template information
     */
    public String getAllTemplates() {

        var request = buildHttpRequestHeader(
                templatingBaseUrl + "/templates/",
                Map.of("Authorization", "Bearer " + tokenBearer),
                RequestMethod.GET
        );

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
        } catch (IOException | InterruptedException e) {
            throw new TemplatingServiceException("Error while trying to access service", e);
        }
    }

    /**
     * Composes file based on the template
     *
     * @param templateId id of the template on service
     * @param mediaType  determines what kind of file is outputed. One of {@link MediaType}
     * @param schema     body to compose file with, must be according to the template schema
     * @return composed file
     */
    public byte[] composeTemplateById(String templateId, MediaType mediaType, String schema) {

        var request = buildHttpRequestHeader(
                templatingBaseUrl + "/template/" + templateId + "/compose",
                Map.of("Accept", mediaType.getString(),
                        "Authorization", "Bearer " + tokenBearer,
                        "Content-Type", "application/json"),
                RequestMethod.POST,
                schema
        );

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
        } catch (IOException | InterruptedException e) {
            throw new TemplatingServiceException("Error while trying to access service", e);
        }
    }


    // Authentication

    private void getAccessToken() {

        String tokenRequest =
                "client_id=" + templatingClientId
                        + "&client_secret=" + templatingSecret
                        + "&grant_type=" + "client_credentials"
                        + "&scope=" + "content-provider-scope";

        HttpRequest oAuth2Request = buildHttpRequestHeader(
                templatingTokenUrl,
                Map.of(
                        "Accept", "application/json",
                        "Content-Type", "application/x-www-form-urlencoded"),
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
        } catch (IOException | InterruptedException e) {
            throw new TemplatingServiceException("Error while trying to get auth token for template service", e);
        }
    }


    // HTTP Header builders

    private HttpRequest buildHttpRequestHeader(String url, Map<String, String> headerMap, RequestMethod requestMethod) {
        return buildHttpRequestHeader(url, headerMap, requestMethod, null);
    }

    private HttpRequest buildHttpRequestHeader(String url, Map<String, String> headerMap, RequestMethod requestMethod, String bodyContent) {
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
