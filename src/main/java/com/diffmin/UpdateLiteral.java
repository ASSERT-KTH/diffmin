package com.diffmin;

import spoon.reflect.code.CtLiteral;

public class UpdateLiteral {
    public static void process(CtLiteral prevNode, CtLiteral newNode) {
        prevNode.setValue(newNode);
    }
}
