package org.gradle.demo.api.evolution;

public class Property<T> {
    private T value;

    public Property(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
