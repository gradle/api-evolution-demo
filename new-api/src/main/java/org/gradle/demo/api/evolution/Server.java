package org.gradle.demo.api.evolution;

class Server {
    private final Property<String> name = new Property<>("new-api");;

    public Property<String> getName() {
        return name;
    }
}
