package com.diffmin;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeUtils;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import spoon.reflect.declaration.CtElement;

public class App
{
    public static String sample(String sampleString) {
        return sampleString;
    }

    public static List<Operation> getOperations(File f1, File f2) throws Exception {
        final List<Operation> operations;
        try {
            operations = new AstComparator().compare(f1, f2).getRootOperations();
        } catch (Exception e) {
            throw e;
        }
        return operations;
    }

    public static ITree patch(File f1, List<Operation> operations) throws Exception {
        final SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
        CtElement prevFile = new AstComparator().getCtType(f1);
        ITree prevTree = scanner.getTree(prevFile);
        for (int i=0; i<operations.size(); ++i) {
            Operation operation = operations.get(i);
            // Currently only update operation is handled
            ITree updatedNode = (ITree)operation.getDstNode().getMetadata(SpoonGumTreeBuilder.GUMTREE_NODE);
            List<ITree> bfsDst = TreeUtils.breadthFirst(prevTree);
            Iterator it = bfsDst.iterator();
            while (it.hasNext()) {
                ITree x = (ITree) it.next();
                if (x.getType() == updatedNode.getType()) {
                    x.setLabel(updatedNode.getLabel());
                }
            }
        }
        return prevTree;
    }

    public static void main( String[] args ) {
        if (args.length != 2) {
            System.out.println("Usage: DiffSpoon <file_1>  <file_2>");
            return;
        }
        List<Operation> operations;
        ITree patchedTree;
        try {
            operations = App.getOperations(new File(args[0]), new File(args[1]));
            patchedTree = App.patch(new File(args[0]), operations);
            System.out.println(patchedTree.toTreeString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
