package com.diffmin;

import com.github.gumtreediff.actions.model.Update;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import spoon.reflect.declaration.CtElement;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;

public class App
{
    public static List<Operation> getOperations(File f1, File f2) throws Exception {
        List<Operation> rootOperations =  new AstComparator().compare(f1, f2).getRootOperations();
        List<Operation> mutableList = new ArrayList<>(rootOperations);

        class OperationSorter implements Comparator<Operation> {
            /**
             *
             * @param o1 First list item
             * @param o2 Second list item
             * @return [-1,0,1] result of comparison of sum of sizes of children list of src and dest node
             */
            @Override
            public int compare(Operation o1, Operation o2) {
                int o1SrcChildren = o1.getSrcNode().getDirectChildren().size();
                int o2SrcChildren = o2.getSrcNode().getDirectChildren().size();
                int o1DestChildren = o1.getDstNode().getDirectChildren().size();
                int o2DestChildren = o2.getDstNode().getDirectChildren().size();
                return Double.compare(o1SrcChildren + o1DestChildren, o2SrcChildren + o2DestChildren);
            }
        }

        Collections.sort(mutableList, new OperationSorter());
        Collections.reverse(mutableList);

        return mutableList;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
