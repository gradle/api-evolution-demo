package org.gradle.demo.api.evolution;

import groovy.lang.DelegatingMetaClass;
import groovy.lang.GroovySystem;
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.callsite.AbstractCallSite;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.objectweb.asm.MethodVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

class GroovyPropertyReplacement<T, V> implements Replacement {
    private static final Logger LOGGER = LoggerFactory.getLogger(GroovyPropertyReplacement.class);
    private final Class<T> type;
    private final Class<V> propertyType;
    private final String propertyName;
    private final Function<? super T, ? extends V> getterReplacement;
    private final BiConsumer<? super T, ? super V> setterReplacement;

    public GroovyPropertyReplacement(Class<T> type, Class<V> propertyType, String propertyName, Function<? super T, ? extends V> getterReplacement, BiConsumer<? super T, ? super V> setterReplacement) {
        this.type = type;
        this.propertyType = propertyType;
        this.propertyName = propertyName;
        this.getterReplacement = getterReplacement;
        this.setterReplacement = setterReplacement;
    }

    @Override
    public boolean replaceByteCodeIfMatches(int opcode, String owner, String name, String desc, boolean itf, int index, MethodVisitor mv) {
        return false;
    }

    @Override
    public Optional<CallSite> decorateCallSite(CallSite callSite) {
        if (callSite.getName().equals(propertyName)) {
            return Optional.of(new AbstractCallSite(callSite) {
                @Override
                @SuppressWarnings("unused")
                public Object callGetProperty(Object receiver) throws Throwable {
                    return getterReplacement.apply((T) receiver);
                }
            });
        }
        return Optional.empty();
    }

    @Override
    public void decorateMetaClass() {
        MetaClass metaClass = GroovySystem.getMetaClassRegistry().getMetaClass(type);
        GroovySystem.getMetaClassRegistry().setMetaClass(type, new PropertySetterMetaClass<T, V>(propertyName, setterReplacement, metaClass));
    }

    private static class PropertySetterMetaClass<T, V> extends DelegatingMetaClass {
        private final String propertyName;
        private final BiConsumer<? super T, ? super V> setterReplacement;

        public PropertySetterMetaClass(String propertyName, BiConsumer<? super T, ? super V> setterReplacement, MetaClass delegate) {
            super(delegate);
            this.propertyName = propertyName;
            this.setterReplacement = setterReplacement;
        }

        @Override
        @SuppressWarnings("unused")
        public void setProperty(Object object, String property, Object newValue) {
            if (property.equals(propertyName)) {
                setterReplacement.accept((T) object, (V) newValue);
            } else {
                super.setProperty(object, property, newValue);
            }
        }
    }
}
