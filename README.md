# Getting Started

This project was created to offer an easier way to use the templating service from Morphotech
 in other Java projects. 


# Plato Service Helper (Java)

## Maven

```xml
<properties>
    <plato-service-helper.version>2.0.1</plato-service-helper.version>
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
        @Value("${plato-service.base-url}") String baseUrl) {
        return new PlatoService(baseUrl);
    }
}
```
