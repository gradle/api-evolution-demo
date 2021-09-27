package org.gradle.demo.api.evolution;

public class Server {
    private final Property<String> stringProperty = new Property<>("new-api");;
    private final Property<Integer> integerProperty = new Property<>(123);
    private final Property<Double> doubleProperty = new Property<>(123.0);

    public Property<String> getStringProperty() {
        return stringProperty;
    }

    public Property<Integer> getIntegerProperty() {
        return integerProperty;
    }

    public Property<Double> getDoubleProperty() {
        return doubleProperty;
    }
}
