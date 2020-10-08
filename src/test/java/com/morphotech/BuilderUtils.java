package com.morphotech;

public class BuilderUtils {

    public static TemplatingService CreateBasicTemplatingService(
            String baseUrl,
            String tokenUrl,
            String clientId,
            String secret
    ){
        return new TemplatingService(baseUrl, tokenUrl, clientId, secret);
    }

}
