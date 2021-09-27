package org.gradle.demo.api.evolution;

public class Server {
    private String stringProperty = "old-api";
    private int integerProperty = 123;
    private double doubleProperty = 123.0;

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public int getIntegerProperty() {
        return integerProperty;
    }

    public void setIntegerProperty(int integerProperty) {
        this.integerProperty = integerProperty;
    }

    public double getDoubleProperty() {
        return doubleProperty;
    }

    public void setDoubleProperty(double doubleProperty) {
        this.doubleProperty = doubleProperty;
    }
}
