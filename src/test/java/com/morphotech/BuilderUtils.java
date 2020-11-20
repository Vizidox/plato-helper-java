package com.morphotech;

public class BuilderUtils {

    public static PlatoService CreateBasicTemplatingService(
            String baseUrl,
            String tokenUrl,
            String clientId,
            String secret
    ){
        return new PlatoService(baseUrl, tokenUrl, clientId, secret);
    }

}
