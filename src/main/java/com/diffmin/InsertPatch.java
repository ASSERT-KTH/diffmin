package com.diffmin;

import java.util.Iterator;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtFormalTypeDeclarer;
import spoon.reflect.declaration.CtMethod;

/**
 * Applies insert patch to CtElements.
 */
public class InsertPatch {

    private CtElement prevFileElement;
    private CtElement insertedNode;

    /**
     * Set up node insertion.
     *
     * @param insertedNode Node which has to be inserted
     */
    public InsertPatch(CtElement prevFileElement, CtElement insertedNode) {
        this.prevFileElement = prevFileElement;
        this.insertedNode = insertedNode;
    }

    /**
     * Inserts `insertedNode` into CtElement tree.
     */
    public void process() {
        CtElement uniqueParent = this.insertedNode.getParent();
        while (!(uniqueParent instanceof CtClass) && !(uniqueParent instanceof CtMethod)) {
            uniqueParent = uniqueParent.getParent();
        }
        Iterator originalElementIt = this.prevFileElement.descendantIterator();
        while (originalElementIt.hasNext()) {
            CtElement originalElement = (CtElement) originalElementIt.next();
            if (
                    (originalElement instanceof CtMethod || originalElement instanceof CtClass)
                    && ((CtFormalTypeDeclarer) originalElement).getSimpleName()
                    .equals(((CtFormalTypeDeclarer) uniqueParent).getSimpleName())
            ) {
                originalElement.replace(uniqueParent);
                ((CtFormalTypeDeclarer) originalElement).setSimpleName(
                        ((CtFormalTypeDeclarer) uniqueParent).getSimpleName()
                );
                return;
            }
        }
    }

}
