package org.gradle.demo.api.evolution;

public class Server {
    private String stringProperty = "old-api";

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }
}
