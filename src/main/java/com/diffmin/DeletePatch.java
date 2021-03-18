package com.diffmin;

import java.util.List;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtPath;

/**
 * Applies delete patch to CtElements.
 */
public class DeletePatch {

    private CtModel modelToBePatched;
    private CtElement removedNode;

    /**
     * Custom exception when more than one element is returned.
     */
    class WrongNumberOfElementReturned extends Exception {
        WrongNumberOfElementReturned(String message) {
            super(message);
        }
    }

    /**
     * Set up node removal.
     *
     * @param removedNode Node which has to be deleted
     */
    public DeletePatch(CtModel modelToBePatched, CtElement removedNode) {
        this.modelToBePatched = modelToBePatched;
        this.removedNode = removedNode;
    }

    /**
     * Removes `removedNode` from CtElement tree.
     */
    public void process() throws WrongNumberOfElementReturned {
        CtPath removedNodePath = this.removedNode.getPath();
        // get(0) is used under that assumption that only one element will be returned
        // corresponding to a CtPath
        List<CtElement> elementsToBeDeleted = removedNodePath.evaluateOn(
            this.modelToBePatched.getRootPackage()
        );
        if (elementsToBeDeleted.size() == 1) {
            removedNodePath.evaluateOn(this.modelToBePatched.getRootPackage()).get(0).delete();
        }
        else if (elementsToBeDeleted.size() > 1) {
            throw new WrongNumberOfElementReturned("More than one element is found for deletion");
        }
    }
}
