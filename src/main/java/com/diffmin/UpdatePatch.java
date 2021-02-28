package com.diffmin;

import spoon.reflect.declaration.CtClass;
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
     * Set up node updating.
     *
     * @param prevNode Node which has to be updated
     * @param newNode Node which replaces `prevNode`
     */
    public UpdatePatch(CtElement prevNode, CtElement newNode) {
        this.prevNode = prevNode;
        this.newNode = newNode;
    }
    
    /**
     * Replaces the previous node with the new node.
     */
    public void process() {
        if (this.prevNode instanceof CtClass && this.newNode instanceof CtClass) {
            // Class name is not updated while replacing a CtClass element
            ((CtClass<?>) this.prevNode).setSimpleName(((CtClass<?>) this.newNode).getSimpleName());
        }
        this.prevNode.replace(this.newNode);
    }
}
