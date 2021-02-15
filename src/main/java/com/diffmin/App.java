package com.diffmin;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;
import spoon.support.reflect.code.CtLiteralImpl;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class App
{
    public static List<Operation> getOperations(File f1, File f2) throws Exception {
        return new AstComparator().compare(f1, f2).getRootOperations();
    }

    public static CtElement patch(File f1, List<Operation> operations) throws Exception {
        CtElement prevFile = new AstComparator().getCtType(f1);
        for (Operation operation : operations) {
            CtElement updatedNode = operation.getDstNode();
            Iterator it = prevFile.descendantIterator();
            while (it.hasNext()) {
                CtElement element = (CtElement) it.next();
                if (element instanceof CtLiteral) {
                    UpdateLiteral ul = new UpdateLiteral((CtLiteral<CtLiteralImpl>) updatedNode);
                    ul.process((CtLiteral<CtLiteralImpl>) element);
                }
            }
        }
        return prevFile;
    }

    public static void main( String[] args ) {
        if (args.length != 2) {
            System.out.println("Usage: DiffSpoon <file_1>  <file_2>");
            return;
        }
        List<Operation> operations;
        CtElement patchedCtElement;
        try {
            operations = App.getOperations(new File(args[0]), new File(args[1]));
            patchedCtElement = App.patch(new File(args[0]), operations);
            System.out.println(patchedCtElement.prettyprint());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
