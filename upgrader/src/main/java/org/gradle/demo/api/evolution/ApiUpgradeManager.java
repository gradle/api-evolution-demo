package org.gradle.demo.api.evolution;

import com.google.common.io.Resources;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.callsite.CallSiteArray;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.ASMifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ApiUpgradeManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUpgradeManager.class);

    private static final Type[] EMPTY = {};
    private static final ApiUpgradeManager INSTANCE = new ApiUpgradeManager();

    public static ApiUpgradeManager getInstance() {
        return INSTANCE;
    }

    private final List<Replacement> replacements = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static <T> T invokeReplacement(int methodReplacementIndex, Object receiver, Object... args) {
        MethodReplacement<T> methodReplacement = (MethodReplacement<T>) INSTANCE.replacements.get(methodReplacementIndex);
        return (T) methodReplacement.invokeReplacement(receiver, args);
    }

    public static void decorateCallSiteArray(CallSiteArray callSites) {
        for (CallSite callSite : callSites.array) {
            for (Replacement replacement : INSTANCE.replacements) {
                replacement.decorateCallSite(callSite).ifPresent(decoreated ->
                    callSites.array[callSite.getIndex()] = decoreated
                );
            }
        }
    }

    public interface MethodReplacer<T> {
        <T> void replaceWith(ReplacementLogic<T> method);
    }

    public <T> MethodReplacer<T> matchMethod(Type type, Type returnType, String methodName, Type... argumentTypes) {
        return new MethodReplacer<T>() {
            @Override
            public <T> void replaceWith(ReplacementLogic<T> replacement) {
                replacements.add(new MethodReplacement<>(type, returnType, methodName, argumentTypes, replacement));
            }
        };
    }

    interface GetterReplacer<T, P> {
        void replaceWith(Function<? super T, ? extends P> getter) throws NoSuchMethodException;
    }

    interface SetterReplacer<T, P> {
        void replaceWith(BiConsumer<? super T, ? super P> setter) throws NoSuchMethodException;
    }

    interface PropertyReplacer<T, P> {
        void replaceWith(Function<? super T, ? extends P> getter, BiConsumer<? super T, ? super P> setter) throws NoSuchMethodException;
    }

    public <T, P> GetterReplacer<T, P> matchGetter(Class<T> type, Class<P> propertyType, String getterName) {
        return new GetterReplacer<T, P>() {
            @Override
            public void replaceWith(Function<? super T, ? extends P> getterReplacement) throws NoSuchMethodException {
                addGetterReplacement(type, propertyType, getterName, getterReplacement);
            }
        };
    }

    public <T, P> SetterReplacer<T, P> matchSetter(Class<T> type, Class<P> propertyType, String setterName) {
        return new SetterReplacer<T, P>() {
            @Override
            public void replaceWith(BiConsumer<? super T, ? super P> setterReplacement) throws NoSuchMethodException {
                addSetterReplacement(type, propertyType, setterName, setterReplacement);
            }
        };
    }

    public <T, P> PropertyReplacer<T, P> matchProperty(Class<T> type, Class<P> propertyType, String getterName, String setterName) {
        return new PropertyReplacer<T, P>() {
            @Override
            public void replaceWith(
                Function<? super T, ? extends P> getterReplacement,
                BiConsumer<? super T, ? super P> setterReplacement
            ) {
                addGetterReplacement(type, propertyType, getterName, getterReplacement);
                addSetterReplacement(type, propertyType, setterName, setterReplacement);
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <T, P> void addGetterReplacement(Class<T> type, Class<P> propertyType, String getterName, Function<? super T, ? extends P> getterReplacement) {
        replacements.add(new MethodReplacement<P>(
            Type.getType(type),
            Type.VOID_TYPE,
            getterName,
            new Type[]{Type.getType(propertyType)},
            (receiver, arguments) -> getterReplacement.apply((T) receiver)));
    }

    @SuppressWarnings("unchecked")
    private <T, P> void addSetterReplacement(Class<T> type, Class<P> propertyType, String setterName, BiConsumer<? super T, ? super P> setterReplacement) {
        replacements.add(new MethodReplacement<Void>(
            Type.getType(type),
            Type.VOID_TYPE,
            setterName,
            new Type[]{Type.getType(propertyType)},
            (receiver, arguments) -> {
                setterReplacement.accept((T) receiver, (P) arguments[0]);
                return null;
            }));
    }

    public void implementReplacements(Type type) throws IOException, ReflectiveOperationException {
        LOGGER.info("Transforming " + type);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // creates the ASM ClassReader which will read the class file
        ClassReader classReader = new ClassReader(Resources.toByteArray(Resources.getResource(type.getInternalName() + ".class")));
        // creates the ASM ClassWriter which will create the transformed class
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        // creates the ClassVisitor to do the byte code transformations
        ClassVisitor classVisitor = new ApiUpgraderClassVisitor(replacements, classWriter);
        // reads the class file and apply the transformations which will be written into the ClassWriter
        classReader.accept(classVisitor, 0);

        // gets the bytes from the transformed class
        byte[] bytes = classWriter.toByteArray();
        // writes the transformed class to the file system - to analyse it (e.g. javap -verbose)
        File out = new File("build/" + type.getClassName() + "\\$Transformed.class");
        new FileOutputStream(out).write(bytes);
        ASMifier.main(new String[]{out.getAbsolutePath()});

        // inject the transformed class into the current class loader
        Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
        defineClass.setAccessible(true);
        defineClass.invoke(classLoader, null, bytes, 0, bytes.length);
    }
}
