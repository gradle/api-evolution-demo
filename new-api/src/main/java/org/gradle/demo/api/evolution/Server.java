package org.gradle.demo.api.evolution;

public class Server {
    private final Property<String> testProperty = new Property<>("new-api");;

    public Property<String> getTestProperty() {
        return testProperty;
    }
}
