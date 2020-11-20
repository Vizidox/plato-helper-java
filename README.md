# Getting Started

This project was created to offer an easier way to use the templating service from Morphotech
 in other Java projects. 


# Plato Service Helper (Java)

## Maven

```xml
<properties>
    <plato-service-helper.version>1.0.0</plato-service-helper.version>
</properties>
...
<dependency>
    <groupId>com.morphotech</groupId>
    <artifactId>plato-helper-java</artifactId>
    <version>${plato-service-helper.version}</version>
</dependency>
```

## Configuration

#### Recommended creating a service (or similar)

```java
@Configuration
public class PlatoConfig {

    @Bean
    public PlatoService platoService(
        @Value("${plato-service.base-url}") String baseUrl,
        @Value("${plato-service.token-url}") String tokenUrl,
        @Value("${plato-service.credentials.client-id}") String clientId,
        @Value("${plato-service.credentials.secret}") String secret) {
        return new PlatoService(baseUrl, tokenUrl, clientId, secret);
    }
}
```
