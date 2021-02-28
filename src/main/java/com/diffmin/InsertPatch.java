package com.diffmin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        List<CtElement> parents = this.generateParentList(this.insertedNode);
        for (int parent = 0; parent < parents.size(); ++parent) {
            Iterator originalElementIt = this.prevFileElement.descendantIterator();
            while (originalElementIt.hasNext()) {
                CtElement originalElement = (CtElement) originalElementIt.next();
                if (
                        parents.get(parent)
                                .getShortRepresentation()
                                .equals(originalElement.getShortRepresentation())
                ) {
                    originalElement.replace(parents.get(parent).clone());
                    break;
                }
            }
        }
    }

    /**
     * Return a list of parents of a particular node.
     *
     * @param node node whose parents needs to be appended to a list
     * @return list of parents
     */
    public List<CtElement> generateParentList(CtElement node) {
        List<CtElement> l = new ArrayList<>();
        CtElement toBeInserted = node;
        while (toBeInserted != null) {
            l.add(toBeInserted);
            toBeInserted = toBeInserted.getParent();
        }
        return l;
    }
}
