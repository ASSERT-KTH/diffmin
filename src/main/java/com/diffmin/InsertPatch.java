package com.diffmin;

import java.util.Iterator;
import spoon.reflect.declaration.CtElement;

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
        Iterator originalElementIt = this.prevFileElement.descendantIterator();
        while (originalElementIt.hasNext()) {
            CtElement originalElement = (CtElement) originalElementIt.next();
            CtElement tobeInserted = this.insertedNode;
            while (tobeInserted != null && tobeInserted.getParent() != null) {
                if (
                        tobeInserted
                            .getShortRepresentation()
                            .equals(originalElement.getShortRepresentation())
                ) {
                    originalElement.replace(tobeInserted);
                    break;
                }
                else {
                    tobeInserted = tobeInserted.getParent();
                }
            }
        }
    }
}
