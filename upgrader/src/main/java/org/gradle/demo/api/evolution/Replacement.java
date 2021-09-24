package org.gradle.demo.api.evolution;

import org.objectweb.asm.MethodVisitor;

public interface Replacement {
    /**
     * Replace the given instruction if it matches the replacement.
     *
     * @return {@code true} if the instruction has been replaced.
     */
    boolean replaceIfMatches(int opcode, String owner, String name, String desc, boolean itf, int index, MethodVisitor mv);
}
