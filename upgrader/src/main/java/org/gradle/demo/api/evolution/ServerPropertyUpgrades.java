package org.gradle.demo.api.evolution;

import org.objectweb.asm.Type;

import javax.annotation.Nullable;

import static org.objectweb.asm.Type.getType;

class ServerPropertyUpgrades {
    UpgradeInstanceMethodCall<Server> createGetterUpgrader() {
        return new UpgradeInstanceMethodCall<Server>(getType(Server.class), "getTestProperty", getType(String.class)) {
            @Override
            public Object callNewApi(Server target, Object... args) throws Exception {
                return target.getTestProperty().get();
            }
        };
    }

    UpgradeInstanceMethodCall<Server> createSetterUpgrader() {
        return new UpgradeInstanceMethodCall<Server>(getType(Server.class), "setTestProperty", getType(String.class), getType(String.class)) {
            @Override
            public Object callNewApi(Server target, Object... args) throws Exception {
                target.getTestProperty().set((String) args[0]);
                return null;
            }
        };
    }
}
