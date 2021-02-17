package com.diffmin;

import com.github.gumtreediff.actions.model.Update;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import org.apache.commons.compress.utils.Lists;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLiteral;
import spoon.reflect.declaration.CtElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class App
{
    public static List<Operation> getOperations(File f1, File f2) throws Exception {
        List rootOperations = new AstComparator().compare(f1, f2).getRootOperations();
        List reversedList = new ArrayList();
        for (int i=rootOperations.size()-1; i>=0; --i) {
            reversedList.add(rootOperations.get(i));
        }
        return reversedList;
    }

    public static CtElement patch(File f1, List<Operation> operations) throws Exception {
        CtElement prevFileElement = new AstComparator().getCtType(f1);
        for (Operation operation : operations) {
            if (operation.getAction() instanceof Update) {
                CtElement updatedNodeSrc = operation.getSrcNode();
                CtElement updatedNodeDest = operation.getDstNode();
                Iterator it = prevFileElement.descendantIterator();
                while (it.hasNext()) {
                    CtElement element = (CtElement) it.next();
//                    if (updatedNodeSrc instanceof CtLiteral) {
                        System.out.println("----------");
                        System.out.println("Op src " + updatedNodeSrc.prettyprint());
                        System.out.println("Op dest " + updatedNodeDest.prettyprint());
                        System.out.println("Being edited " + element.prettyprint());
                        System.out.println(updatedNodeSrc.equals(element) + "  " + updatedNodeSrc.getDirectChildren() + " " + element.getDirectChildren());
                        System.out.println("----------");
//                    }
                    if (updatedNodeSrc.equals(element)) {
                        UpdatePatch up = new UpdatePatch(element, updatedNodeDest);
                        up.process();
                    }
                }
            }
        }
        return prevFileElement;
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
            for(int i=0;i<operations.size();++i) {
                System.out.println(operations.get(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
