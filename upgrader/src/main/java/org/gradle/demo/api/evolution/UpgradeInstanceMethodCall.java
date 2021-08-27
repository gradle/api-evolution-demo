package org.gradle.demo.api.evolution;

import com.google.common.collect.ImmutableList;
import org.objectweb.asm.Type;

import javax.annotation.Nullable;
import java.util.List;

public abstract class UpgradeInstanceMethodCall<T> {
    private final Type owner;
    private final String name;
    private final Type returnType;
    private final List<Type> argumentTypes;

    public UpgradeInstanceMethodCall(Type owner, String name, Type returnType, Type... argumentTypes) {
        this.owner = owner;
        this.name = name;
        this.returnType = returnType;
        this.argumentTypes = ImmutableList.copyOf(argumentTypes);
    }

    @Nullable
    public abstract Object callNewApi(T target, Object... args) throws Exception;

    public Type getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public List<Type> getArgumentTypes() {
        return argumentTypes;
    }
}
