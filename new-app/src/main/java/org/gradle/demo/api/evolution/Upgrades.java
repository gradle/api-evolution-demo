package org.gradle.demo.api.evolution;

import org.objectweb.asm.Type;

import java.io.IOException;

public class Upgrades {
    public Upgrades() {
        ApiUpgradeManager.getInstance()
            .matchMethod(
                Type.getType(Server.class),
                Type.VOID_TYPE,
                "setTestProperty",
                Type.getType(String.class)
            )
            .replaceWith((receiver, args) -> {
                ((Server) receiver).getTestProperty().set((String) args[0]);
                return null;
            });

        ApiUpgradeManager.getInstance()
            .matchMethod(
                Type.getType(Server.class),
                Type.getType(String.class),
                "getTestProperty"
            )
            .replaceWith((receiver, args) -> {
                return ((Server) receiver).getTestProperty().get();
            });
    }

    public void init() throws ReflectiveOperationException, IOException {
        for (String name : new String[]{"JavaClient", "KotlinClient", "DynamicGroovyClient", "StaticGroovyClient"}) {
            ApiUpgradeManager.getInstance().implementReplacements(Type.getType("L" + getClass().getPackage().getName().replace('.', '/') + "/" + name + ";"));
        }
    }
}
