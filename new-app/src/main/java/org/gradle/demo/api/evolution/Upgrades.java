package org.gradle.demo.api.evolution;

import org.objectweb.asm.Type;

import java.io.IOException;

public class Upgrades {
    public Upgrades() {
        ApiUpgradeManager.getInstance()
            .matchProperty(Server.class, String.class, "stringProperty")
            .replaceWith(
                receiver -> receiver.getStringProperty().get(),
                (receiver, value) -> receiver.getStringProperty().set(value)
            );
        ApiUpgradeManager.getInstance()
            .matchProperty(Server.class, int.class, "integerProperty")
            .replaceWith(
                receiver -> receiver.getIntegerProperty().get(),
                (receiver, value) -> receiver.getIntegerProperty().set(value)
            );
        ApiUpgradeManager.init();
    }

    public void init() throws ReflectiveOperationException, IOException {
        for (String name : new String[]{"JavaClient", "KotlinClient", "DynamicGroovyClient", "StaticGroovyClient"}) {
            ApiUpgradeManager.getInstance().implementReplacements(Type.getType("L" + getClass().getPackage().getName().replace('.', '/') + "/" + name + ";"));
        }
    }
}
