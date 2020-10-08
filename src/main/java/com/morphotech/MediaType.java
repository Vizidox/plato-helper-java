package com.morphotech;

public enum MediaType {

    APPLICATION_PDF("application/pdf"),
    IMAGE_PNG("image/png"),
    TEXT_HTML("text/html");

    private final String stringType;

    MediaType(String stringType) {
        this.stringType = stringType;
    }

    public String getString() {
        return stringType;
    }
}
