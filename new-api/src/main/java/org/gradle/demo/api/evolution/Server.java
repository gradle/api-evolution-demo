package org.gradle.demo.api.evolution;

public class Server {
    private final Property<String> stringProperty = new Property<>("new-api");;

    public Property<String> getStringProperty() {
        return stringProperty;
    }
}
