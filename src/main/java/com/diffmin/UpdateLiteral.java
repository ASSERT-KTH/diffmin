package com.diffmin;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtLiteral;

public class UpdateLiteral extends AbstractProcessor<CtLiteral> {

    CtLiteral newLiteralNode;

    public UpdateLiteral(CtLiteral newLiteralNode) {
        this.newLiteralNode = newLiteralNode;
    }

    @Override
    public void process(CtLiteral candidate) {
        ((CtLiteral<CtLiteral>) candidate).setValue(this.newLiteralNode);
    }
}
