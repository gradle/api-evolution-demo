package org.gradle.demo.api.evolution;

public class Server {
    private String testProperty = "old-api";

    public String getTestProperty() {
        return testProperty;
    }

    public void setTestProperty(String testProperty) {
        this.testProperty = testProperty;
    }
}
