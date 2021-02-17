package com.diffmin;

import spoon.reflect.declaration.CtElement;

/**
 * Applies update patch to CtElements. Types taken care of:
 * <a href="https://github.com/INRIA/spoon/blob/master/src/main/java/spoon/reflect/code/CtLiteral.java">CtLiteral</a>
 * <a href="https://github.com/INRIA/spoon/blob/master/src/main/java/spoon/reflect/code/CtInvocation.java">CtInvocation</a>
 */
public class UpdatePatch {
    /**
     * Replaces the previous node with the new node.
     *
     * @param prevNode Node which has to be replaced
     * @param newNode Replacement node
     */
    public static void process(CtElement prevNode, CtElement newNode) {
        prevNode.replace(newNode);
    }
}
