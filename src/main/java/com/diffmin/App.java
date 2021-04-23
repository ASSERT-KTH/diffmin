package com.diffmin;

import com.diffmin.util.Pair;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Update;
import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
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
import spoon.compiler.Environment;
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
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.reflect.visitor.PrettyPrinter;
import spoon.support.StandardEnvironment;

/**
 * Entry point of the project. Computes the edit script and uses it to patch the.
 */
public class App {

    private List<CtElement> deletePatches = new ArrayList<>();
    private List<Pair<CtElement, CtElement>> updatePatches = new ArrayList<>();
    private Set<ImmutableTriple<Integer, CtElement, CtElement>> insertPatches = new HashSet<>();

    /**
     * Constructor of the kernel to generate and apply patch.
     *
     * @param prevFilePath Path of the previous version of the file which needs to be modified
     */
    public App(String prevFilePath) {
        final Launcher launcher = new Launcher();
        launcher.addInputResource(prevFilePath);
    }

    /**
     * Returns the root package of the file.
     *
     * @param file File whose all {@link CtPackage} needs to returned
     * @return Root package of the file
     * @throws FileNotFoundException If the file cannot be found
     */
    public static CtPackage getPackage(File file) throws FileNotFoundException {
        CtModel model = buildModel(file);
        return model.getRootPackage();
    }

    /**
     * Computes the diff between the two files and returns the diff and the model to be patched.
     *
     * @param prevFile Previous version of the file
     * @param newFile Modified version of the file
     * @return A pair (diff, modelToPatch)
     * @throws FileNotFoundException If either file is not found
     */
    public static Pair<Diff, CtModel> computeDiff(File prevFile, File newFile) throws FileNotFoundException {
        CtElement prevPackage = getPackage(prevFile);
        CtElement newPackage = getPackage(newFile);
        Diff diff = new AstComparator().compare(prevPackage, newPackage);
        CtModel modelToBeModified = prevPackage.getFactory().getModel();
        return new Pair<>(diff, modelToBeModified);
    }

    /**
     * @param file File with Java source code
     * @return A built model
     * @throws FileNotFoundException Exception raised via {@link SpoonResourceHelper}
     */
    static CtModel buildModel(File file) throws FileNotFoundException {
        final SpoonResource resource = SpoonResourceHelper.createResource(file);
        final Launcher launcher = new Launcher();

        Environment env = launcher.getEnvironment();
        env.setCommentEnabled(false); // TODO enable comments
        env.setPrettyPrinterCreator(() -> {
            DefaultJavaPrettyPrinter printer = new DefaultJavaPrettyPrinter(env);
            printer.setIgnoreImplicit(false); // required to NOT print e.g. implicit "this"
            return printer;
        });

        launcher.addInputResource(resource);
        return launcher.buildModel();
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

        // Note: Must explicitly create our configured pretty printer, as spoon-9.0.0 has that
        // CompilationUnit.prettyprint() always uses the auto-import pretty-printer, and not
        // our custom configured one.
        PrettyPrinter printer = cu.getFactory().getEnvironment().createPrettyPrinter();
        return printer.prettyprint(cu);
    }

    /**
     * Generate list of patches for each individual operation type - {@link OperationKind}.
     *
     * @param diff Diff to generate patch for
     */
    public void generatePatch(Diff diff) {
        List<Operation> operations = diff.getRootOperations();
        SpoonMapping mapping = SpoonMapping.fromGumTreeMapping(diff.getMappingsComp());

        for (Operation<?> operation : operations) {
            if (operation.getAction() instanceof Delete) {
                CtElement removedNode = operation.getSrcNode();
                deletePatches.add(removedNode);
            }
            else if (operation.getAction() instanceof Update) {
                CtElement srcNode = operation.getSrcNode();
                CtElement dstNode = operation.getDstNode();
                updatePatches.add(new Pair<>(srcNode, dstNode));
            }
            else if (operation.getAction() instanceof Insert) {
                CtElement insertedNode = operation.getSrcNode();
                CtElement insertedNodeParent = insertedNode.getParent();
                List<? extends CtElement> newCollectionList =
                        getCollectionElementList(insertedNode);
                CtElement parentElementInPrevModel = mapping.get(insertedNodeParent);

                int srcNodeIndex = IntStream.range(0, newCollectionList.size())
                        .filter(i -> newCollectionList.get(i) == insertedNode)
                        .findFirst()
                        .getAsInt();

                insertPatches.add(
                        new ImmutableTriple<>(srcNodeIndex, insertedNode, parentElementInPrevModel)
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
    private static void applyInsertion(ImmutableTriple<Integer, CtElement, CtElement> insert) {
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
            Pair<Diff, CtModel> diffAndModel = App.computeDiff(new File(args[0]), new File(args[1]));
            app.generatePatch(diffAndModel.getFirst());
            app.applyPatch();
            CtModel patchedCtModel = diffAndModel.getSecond();
            System.out.println(app.displayModifiedModel(patchedCtModel));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
