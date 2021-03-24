package com.diffmin;

import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Update;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import spoon.Launcher;
import spoon.javadoc.internal.Pair;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;

/**
 * Entry point of the project. Computes the edit script and uses it to patch the.
 */
public class App {

    private List<CtElement> deletePatches = new ArrayList<>();
    private List<Pair<CtElement, CtElement>> updatePatches = new ArrayList<>();
    public CtModel modelToBeModified;

    /**
     * Constructor of the kernel to generate and apply patch.
     *
     * @param prevFilePath Path of the previous version of the file which needs to be modified
     */
    public App(String prevFilePath) {
        final Launcher launcher = new Launcher();
        launcher.addInputResource(prevFilePath);
        this.modelToBeModified = launcher.buildModel();
    }

    /**
     * Computes the list of operations which is used for patching `f2`.
     *
     * @param prevFile Previous version of the file
     * @param newFile Modified version of the file
     * @return List of operations in the edit script
     * @throws Exception Exception raised via {@link AstComparator}
     */
    public List<Operation> getOperations(File prevFile, File newFile) throws Exception {
        return new AstComparator().compare(prevFile, newFile).getRootOperations();
    }

    /**
     * Return the node in the prev file model for further modification.
     *
     * @param element node which has to be located in prev file model
     * @return located node in the prev file model
     */
    private List<CtElement> getElementToBeModified(CtElement element) {
        return element.getPath().evaluateOn(this.modelToBeModified.getRootPackage());
    }

    /**
     * Generate list of patches for each individual operation type - {@link OperationKind}.
     *
     * @param operations List of operations which will govern how `prevFile` will be patched
     */
    public void generatePatch(List<Operation> operations) {

        for (Operation operation : operations) {
            if (operation.getAction() instanceof Delete) {
                CtElement removedNode = operation.getSrcNode();
                List<CtElement> elementsToBeDeleted = this.getElementToBeModified(removedNode);
                this.deletePatches.addAll(elementsToBeDeleted);
            }
            else if (operation.getAction() instanceof Update) {
                CtElement srcNode = operation.getSrcNode();
                CtElement dstNode = operation.getDstNode();
                List<CtElement> elementsToBeUpdated = this.getElementToBeModified(srcNode);
                for (int i = 0; i < elementsToBeUpdated.size(); ++i) {
                    updatePatches.add(new Pair(elementsToBeUpdated.get(0), dstNode));
                }
            }
        }
    }

    /**
     * Apply all the patches generated.
     */
    public void applyPatch() {
        for (CtElement element : deletePatches) {
            element.delete();
        }
        for (Pair<CtElement, CtElement> update : updatePatches) {
            CtElement prevNode = update.a;
            CtElement newNode = update.b;
            prevNode.replace(newNode);
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
