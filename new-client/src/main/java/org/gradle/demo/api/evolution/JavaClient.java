package org.gradle.demo.api.evolution;

public class JavaClient {
    public static void main() {
        Server server = new Server();

        server.getStringProperty().set("lajos");
        System.out.println(server.getStringProperty().get());

        server.getIntegerProperty().set(321);
        System.out.println(server.getIntegerProperty().get());

        server.getDoubleProperty().set(321.0);
        System.out.println(server.getDoubleProperty().get());
    }
}
