package org.gradle.demo.api.evolution;

import org.objectweb.asm.Type;

import java.io.IOException;

public class Upgrades {
    public void init() throws ReflectiveOperationException, IOException {
        ApiUpgradeManager upgradeManager = new ApiUpgradeManager();
        upgradeManager
            .matchProperty(Server.class, String.class, "stringProperty")
            .replaceWith(
                receiver -> receiver.getStringProperty().get(),
                (receiver, value) -> receiver.getStringProperty().set(value)
            );
        upgradeManager
            .matchProperty(Server.class, int.class, "integerProperty")
            .replaceWith(
                receiver -> receiver.getIntegerProperty().get(),
                (receiver, value) -> receiver.getIntegerProperty().set(value)
            );
        upgradeManager
            .matchProperty(Server.class, double.class, "doubleProperty")
            .replaceWith(
                receiver -> receiver.getDoubleProperty().get(),
                (receiver, value) -> receiver.getDoubleProperty().set(value)
            );
        for (String name : new String[]{"JavaClient", "KotlinClient", "DynamicGroovyClient", "StaticGroovyClient"}) {
            upgradeManager.implementReplacements(Type.getType("L" + getClass().getPackage().getName().replace('.', '/') + "/" + name + ";"));
        }
        upgradeManager.init();
    }
}
