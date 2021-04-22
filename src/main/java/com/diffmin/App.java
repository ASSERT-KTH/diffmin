package com.diffmin;

import com.diffmin.util.Pair;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Update;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.OperationKind;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import spoon.Launcher;
import spoon.compiler.SpoonResource;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;

/**
 * Entry point of the project. Computes the edit script and uses it to patch the.
 */
public class App {

    private List<CtElement> deletePatches = new ArrayList<>();
    private List<Pair<CtElement, CtElement>> updatePatches = new ArrayList<>();
    private Set<ImmutableTriple<Integer, CtElement, CtElement>> insertPatches = new HashSet<>();
    public CtModel modelToBeModified;

    /**
     * Constructor of the kernel to generate and apply patch.
     *
     * @param prevFilePath Path of the previous version of the file which needs to be modified
     */
    public App(String prevFilePath) {
        final Launcher launcher = new Launcher();
        launcher.addInputResource(prevFilePath);
        modelToBeModified = launcher.buildModel();
    }

    /**
     * Returns the root package of the file.
     *
     * @param file File whose all {@link CtPackage} needs to returned
     * @return Root package of the file
     * @throws FileNotFoundException Exception raise via {@link SpoonResourceHelper}
     */
    public CtPackage getPackage(File file) throws FileNotFoundException {
        final SpoonResource resource = SpoonResourceHelper.createResource(file);
        final Launcher launcher = new Launcher();
        launcher.addInputResource(resource);
        CtModel model = launcher.buildModel();
        return model.getRootPackage();
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
        CtElement prevPackage = getPackage(prevFile);
        CtElement newPackage = getPackage(newFile);
        return new AstComparator().compare(prevPackage, newPackage).getRootOperations();
    }

    /**
     * Return the node in the prev file model for further modification.
     *
     * @param element node which has to be located in prev file model
     * @return located node in the prev file model
     */
    private List<CtElement> getElementToBeModified(CtElement element) {
        return element.getPath().evaluateOn(modelToBeModified.getRootPackage());
    }

    /**
     * Returns the corresponding list of elements in parent.
     *
     * @param element the element whose parent is a collection
     * @return the list of entities the `element`'s parent contains
     */
    private List<? extends CtElement> getCollectionElementList(CtElement element) {
        switch (element.getRoleInParent()) {
            case STATEMENT:
                return ((CtStatementList) element.getParent()).getStatements();
            case ARGUMENT:
                return ((CtInvocation<?>) element.getParent()).getArguments();
            case TYPE_MEMBER:
                return ((CtClass<?>) element.getParent()).getTypeMembers();
            default:
                throw new UnsupportedOperationException(
                        "Unsupported role: " + element.getRoleInParent()
                );
        }
    }

    /**
     * Pretty prints the model.
     *
     * @param model model to be pretty printed
     * @return patched program
     */
    public String displayModifiedModel(CtModel model) {
        CtType<?> firstType = model.getAllTypes().stream().findFirst().get();
        CtCompilationUnit cu = firstType.getFactory().CompilationUnit().getOrCreate(firstType);
        return cu.prettyprint();
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
                List<CtElement> elementsToBeDeleted = getElementToBeModified(removedNode);
                deletePatches.addAll(elementsToBeDeleted);
            }
            else if (operation.getAction() instanceof Update) {
                CtElement srcNode = operation.getSrcNode();
                CtElement dstNode = operation.getDstNode();
                List<CtElement> elementsToBeUpdated = getElementToBeModified(srcNode);
                for (CtElement ctElement : elementsToBeUpdated) {
                    updatePatches.add(new Pair<>(ctElement, dstNode));
                }
            }
            else if (operation.getAction() instanceof Insert) {
                CtElement srcNode = operation.getSrcNode();
                CtElement srcNodeParent = srcNode.getParent();
                List<? extends CtElement> newCollectionList =
                        getCollectionElementList(srcNode);
                // Assuming only one element will be matched in the previous model.
                CtElement parentElementInPrevModel =
                        getElementToBeModified(srcNodeParent).get(0);

                int srcNodeIndex = IntStream.range(0, newCollectionList.size())
                        .filter(i -> newCollectionList.get(i) == srcNode)
                        .findFirst()
                        .getAsInt();

                insertPatches.add(
                        new ImmutableTriple<>(srcNodeIndex, srcNode, parentElementInPrevModel)
                );
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
            CtElement prevNode = update.getFirst();
            CtElement newNode = update.getSecond();
            prevNode.replace(newNode);
        }
        insertPatches.forEach(App::applyInsertion);
    }

    /** Apply the insert patch. */
    public static void applyInsertion(ImmutableTriple<Integer, CtElement, CtElement> insert) {
        int where = insert.left;
        CtElement toBeInserted = insert.middle;
        CtElement inWhichElement = insert.right;

        switch (toBeInserted.getRoleInParent()) {
            case STATEMENT:
                ((CtStatementList) inWhichElement)
                        .addStatement(where, (CtStatement) toBeInserted.clone());
                break;
            // FIXME temporary workaround until INRIA/spoon#3885 is merged
            case ARGUMENT:
                List<CtExpression<?>> arguments =
                        ((CtInvocation<?>) inWhichElement).getArguments();
                // If size of the arguments list is 0, we cannot add an argument to it because
                // when it is empty, it is an instance of EmptyClearableList. Thus, instead, we
                // use the `addArgument` API.
                if (arguments.isEmpty()) {
                    ((CtInvocation<?>) inWhichElement)
                            .addArgument((CtExpression<?>) toBeInserted);
                }
                // `addArgument` API cannot be used here as it only appends the new argument
                // and we cannot do precise insertions by providing the index. But a non-empty
                // argument list is an instance of ModelList which can be mutated.
                else {
                    arguments.add(where, (CtExpression<?>) toBeInserted);
                }
                break;
            case TYPE_MEMBER:
                ((CtClass<?>) inWhichElement)
                        .addTypeMemberAt(where, (CtTypeMember) toBeInserted);
                break;
            default:
                throw new UnsupportedOperationException(
                        "Unhandled role: " + toBeInserted.getRoleInParent()
                );
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
            System.out.println(app.displayModifiedModel(patchedCtModel));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
