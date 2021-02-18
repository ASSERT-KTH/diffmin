package com.diffmin;

import spoon.reflect.declaration.CtElement;

/**
 * Applies update patch to CtElements. Types taken care of:
 * <a href="https://github.com/INRIA/spoon/blob/master/src/main/java/spoon/reflect/code/CtLiteral.java">CtLiteral</a>
 * <a href="https://github.com/INRIA/spoon/blob/master/src/main/java/spoon/reflect/code/CtInvocation.java">CtInvocation</a>
 * <a href="https://github.com/INRIA/spoon/blob/master/src/main/java/spoon/reflect/reference/CtTypeReference.java">CtTypeReference</a>
 */
public class UpdatePatch {

    private CtElement prevNode;
    private CtElement newNode;

    /**
     * Replaces the previous node with the new node.
     *
     * @param prevNode Node which has to be replaced
     * @param newNode Replacement node
     */
    public UpdatePatch(CtElement prevNode, CtElement newNode) {
        this.prevNode = prevNode;
        this.newNode = newNode;
    }

    public void process() {
        this.prevNode.replace(this.newNode);
    }
}
