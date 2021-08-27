package org.gradle.demo.api.evolution;

public class JavaClient {
    public static void main() {
        Server server = new Server();
        server.setTestProperty("lajos");
        System.out.println(server.getTestProperty());
    }
}
