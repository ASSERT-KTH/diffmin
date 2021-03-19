package com.diffmin;

import com.github.gumtreediff.actions.model.Delete;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.path.CtPath;

/**
 * Entry point of the project. Computes the edit script and uses it to patch the.
 */
public class App {

    private List<CtElement> deletePatches = new ArrayList<>();
    public CtModel modelToBeModified;

    /**
     * Constructor of the kernel to generate and apply patch.
     *
     * @param leftFilePath Path of the previous version of the file which needs to be modified
     */
    public App(String leftFilePath) {
        final Launcher launcher = new Launcher();
        launcher.addInputResource(leftFilePath);
        this.modelToBeModified = launcher.buildModel();
    }

    /**
     * Computes the list of operations which is used for patching `f2`.
     *
     * @param leftFile Previous version of the file
     * @param rightFile Modified version of the file
     * @return List of operations in the edit script
     * @throws Exception Exception raised via {@link AstComparator}
     */
    public List<Operation> getOperations(File leftFile, File rightFile) throws Exception {
        return new AstComparator().compare(leftFile, rightFile).getRootOperations();
    }

    /**
     * Generate list of patches for each individual operation type - {@link OperationKind}.
     *
     * @param operations List of operations which will govern how `leftFile` will be patched
     */
    public void generatePatch(List<Operation> operations) {

        for (Operation operation : operations) {
            if (operation.getAction() instanceof Delete) {
                CtElement removedNode = operation.getSrcNode();
                CtPath removedNodePath = removedNode.getPath();
                List<CtElement> elementsToBeDeleted = removedNodePath.evaluateOn(
                        this.modelToBeModified.getRootPackage()
                );
                this.deletePatches.addAll(elementsToBeDeleted);
            }
        }
    }

    /**
     * Apply all the patches generated.
     */
    public void applyPatch() {
        for (int i = 0; i < this.deletePatches.size(); ++i) {
            DeletePatch dp = new DeletePatch(this.deletePatches.get(i));
            dp.process();
        }
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
            App app = new App(args[0]);
            List<Operation> operations = app.getOperations(new File(args[0]), new File(args[1]));
            app.generatePatch(operations);
            app.applyPatch();
            CtModel patchedCtModel = app.modelToBeModified;
            if (patchedCtModel.getRootPackage().isEmpty()) {
                System.out.println(patchedCtModel.getRootPackage().prettyprint());
            }
            else {
                // get(0) will return the first element inside the package which is usually a Class
                CtElement patchedCtElement = patchedCtModel.getRootPackage()
                    .getDirectChildren().get(0);
                System.out.println(patchedCtElement.prettyprint());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
