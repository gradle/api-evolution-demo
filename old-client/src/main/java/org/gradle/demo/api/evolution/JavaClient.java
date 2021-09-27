package org.gradle.demo.api.evolution;

public class JavaClient {
    public static void main() {
        Server server = new Server();

        server.setStringProperty("lajos");
        System.out.println(server.getStringProperty());

        server.setIntegerProperty(321);
        System.out.println(server.getIntegerProperty());

        server.setDoubleProperty(321.0);
        System.out.println(server.getDoubleProperty());
    }
}
