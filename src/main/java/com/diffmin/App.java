package com.diffmin;

import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Update;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import spoon.reflect.declaration.CtElement;

/**
 * Entry point of the project. Computes the edit script and uses it to patch the.
 */
public class App {

    /**
     * Computes the list of operations which is used for patching `f2`.
     *
     * @param f1 Previous version of the file
     * @param f2 Modified version of the file
     * @return List of operations in the edit script
     * @throws Exception Exception raised via {@link AstComparator}
     */
    public static List<Operation> getOperations(File f1, File f2) throws Exception {
        List<Operation> rootOperations =  new AstComparator().compare(f1, f2).getRootOperations();
        List<Operation> mutableList = new ArrayList<>(rootOperations);

        class OperationSorter implements Comparator<Operation> {
            /**
             * Sorts the list of operation according to the sizes of nodes attached to each action
             * in descending order.
             *
             * @param o1 First list item
             * @param o2 Second list item
             * @return [-1,0,1] result of comparison of sum of sizes of children list of src and
             * dest node
             */
            @Override
            public int compare(Operation o1, Operation o2) {
                CtElement o1SrcChildren = o1.getSrcNode();
                CtElement o2SrcChildren = o2.getSrcNode();
                CtElement o1DestChildren = o1.getDstNode();
                CtElement o2DestChildren = o2.getDstNode();
                return Double.compare(
                    this.nodeSize(o1SrcChildren) + this.nodeSize(o1DestChildren),
                    this.nodeSize(o2SrcChildren) + this.nodeSize(o2DestChildren)
                );
            }

            /**
             * Calculates the number of direct children of the {@link CtElement} passed.
             *
             * @param ctElement Element whose number of children has to be calculated
             * @return number of direct children
             */
            public int nodeSize(CtElement ctElement) {
                return  ctElement != null ? ctElement.getDirectChildren().size() : 0;
            }
        }

        Collections.sort(mutableList, new OperationSorter());
        Collections.reverse(mutableList);

        return mutableList;
    }

    /**
     * Patches `f1` using the edit script.
     *
     * @param f1 Previous version of the file which needs to be modified
     * @param operations List of operations which will govern how `f1` will be patched
     * @return Updated {@link CtElement} of `f1`
     * @throws Exception Exception raised via {@link AstComparator}
     */
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
            else if (operation.getAction() instanceof Delete) {
                CtElement removedNode = operation.getSrcNode();
                Iterator it = prevFileElement.descendantIterator();
                while (it.hasNext()) {
                    CtElement element = (CtElement) it.next();
                    if (removedNode.equals(element)) {
                        DeletePatch dp = new DeletePatch(element);
                        dp.process();
                    }
                }
            }
            else if (operation.getAction() instanceof Insert) {
                CtElement insertedNode = operation.getSrcNode();
                // Case when the source file is empty so there will be only one insert operation
                // so we just need to return the node attached to the same operation.
                if (prevFileElement == null) {
                    prevFileElement = insertedNode;
                }
                else {
                    InsertPatch ip = new InsertPatch(prevFileElement, insertedNode);
                    ip.process();
                }
            }
        }
        return prevFileElement.getParent().getDirectChildren().get(0);
    }

    /**
     * Runs the patch function and dumps the output in the terminal.
     *
     * @param args Arguments passed via command line
     */
    public static void main(String[] args) {
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
