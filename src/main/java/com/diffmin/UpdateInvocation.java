package com.diffmin;

import spoon.reflect.code.CtInvocation;

public class UpdateInvocation {
    public static void process(CtInvocation prevNode, CtInvocation newNode) {
        prevNode.replace(newNode);
    }
}
