package com.diffmin;

import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;

public class UpdateInvocation {
    public static void process(CtInvocation prevNode, CtInvocation newNode) {
        CtBlock block = (CtBlock) prevNode.getParent();
        block.insertBegin(newNode.clone());
        prevNode.delete();
    }
}
