# Getting Started

This project was created to offer an easier way to use the templating service from Morphotech
 in other Java projects. 


# Templating Service Helper (Java)

## Maven

```xml
<properties>
    <templating-service-helper.version>1.0.0</templating-service-helper.version>
</properties>
...
<dependency>
    <groupId>com.morphotech</groupId>
    <artifactId>templating-helper-java</artifactId>
    <version>${templating-service-helper.version}</version>
</dependency>
```

## Configuration

#### Recommended creating a service (or similar)

```java
@Value("${templating-service.base-url}")
private String templatingBaseUrl;

@Value("${templating-service.token-url}")
private String templatingTokenUrl;

@Value("${templating-service.credentials.client-id}")
private String templatingClientId;

@Value("${templating-service.credentials.secret}")
private String templatingSecret;

...

@bean
public TemplatingService TemplatingService(){
    return new TemplatingService(templatingBaseUrl, templatingTokenUrl, templatingClientId, templatingSecret);
}
```
