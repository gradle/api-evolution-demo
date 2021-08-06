package org.gradle.demo.api.evolution;

public class JavaClient {
    public static void main(String[] args) {
        Server server = new Server();
        server.setName("lajos");
        System.out.println(server.getName());
    }
}
