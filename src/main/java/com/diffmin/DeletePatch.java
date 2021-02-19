package com.diffmin;

import spoon.reflect.declaration.CtElement;

/**
 * Applies delete patch to CtElements.
 */
public class DeletePatch {

    private CtElement removedNode;

    /**
     * Set up node removal.
     *
     * @param removedNode Node which has to be deleted
     */
    public DeletePatch(CtElement removedNode) {
        this.removedNode = removedNode;
    }

    /**
     * Removes `removedNode` from CtElement tree.
     */
    public void process() {
        this.removedNode.delete();
    }
}
