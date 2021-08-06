package org.gradle.demo.api.evolution;

public class Server {
    private String name = "old-api";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
