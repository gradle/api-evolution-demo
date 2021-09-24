package org.gradle.demo.api.evolution;

@FunctionalInterface
public interface ReplacementLogic<T> {
    T execute(Object receiver, Object[] args);
}
