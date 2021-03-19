package com.diffmin;

import spoon.reflect.declaration.CtElement;

/**
 * Applies delete patch to CtElements.
 */
public class DeletePatch {

    private CtElement nodeToBeRemoved;

    /**
     * Set up node removal.
     *
     * @param nodeToBeRemoved Node which has to be deleted
     */
    public DeletePatch(CtElement nodeToBeRemoved) {
        this.nodeToBeRemoved = nodeToBeRemoved;
    }

    /**
     * Removes `nodeToBeRemoved` from the model.
     */
    public void process() {
        this.nodeToBeRemoved.delete();
    }
}
