package com.diffmin;

import com.github.gumtreediff.actions.model.Delete;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import java.io.File;
import java.util.List;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;

/**
 * Entry point of the project. Computes the edit script and uses it to patch the.
 */
public class App {

    /**
     * Computes the list of operations which is used for patching `f2`.
     *
     * @param leftFile Previous version of the file
     * @param rightFile Modified version of the file
     * @return List of operations in the edit script
     * @throws Exception Exception raised via {@link AstComparator}
     */
    public static List<Operation> getOperations(File leftFile, File rightFile) throws Exception {
        return new AstComparator().compare(leftFile, rightFile).getRootOperations();
    }

    /**
     * Patches `leftFile` using the root operations.
     *
     * @param leftFilePath Path of the previous version of the file which needs to be modified
     * @param operations List of operations which will govern how `leftFile` will be patched
     * @return Updated {@link CtModel} containing only `leftFile`
     * @throws Exception Exception raised via {@link AstComparator}
     */
    public static CtModel patch(String leftFilePath, List<Operation> operations) throws Exception {
        final Launcher launcher = new Launcher();
        launcher.addInputResource(leftFilePath);
        CtModel leftModel = launcher.buildModel();
        for (Operation operation : operations) {
            if (operation.getAction() instanceof Delete) {
                CtElement removedNode = operation.getSrcNode();
                DeletePatch dp = new DeletePatch(leftModel, removedNode);
                dp.process();
            }
        }
        return leftModel;
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
        try {
            List<Operation> operations = App.getOperations(new File(args[0]), new File(args[1]));
            CtModel patchedCtModel = App.patch(args[0], operations);
            if (patchedCtModel.getRootPackage().isEmpty()) {
                System.out.println(patchedCtModel.getRootPackage().prettyprint());
            }
            else {
                CtElement patchedCtElement = patchedCtModel.getRootPackage()
                    .getDirectChildren().get(0);
                System.out.println(patchedCtElement.prettyprint());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
