package com.diffmin.util;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.Diff;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import spoon.Launcher;
import spoon.compiler.Environment;
import spoon.compiler.SpoonResource;
import spoon.compiler.SpoonResourceHelper;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtModule;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;
import spoon.reflect.visitor.PrettyPrinter;

/** Utility class for interacting with the {@link CtModel} and computing {@link Diff}. */
public class SpoonUtil {
    /** Override constructor to prevent instantiating of this class (RSPEC-1118). */
    private SpoonUtil() {
        throw new IllegalStateException("Utility classes should not be instantiated");
    }

    /**
     * Returns the root package of the file.
     *
     * @param file File whose all {@link CtPackage} needs to returned
     * @return Root package of the file
     * @throws FileNotFoundException Exception raise via {@link SpoonResourceHelper}
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
     * @return List of operations in the edit script
     * @throws FileNotFoundException Exception raised via {@link AstComparator}
     */
    public static Pair<Diff, CtModel> computeDiff(File prevFile, File newFile)
            throws FileNotFoundException {
        CtElement prevPackage = getPackage(prevFile);
        CtElement newPackage = getPackage(newFile);
        Diff diff = new AstComparator().compare(prevPackage, newPackage);
        CtModel modelToBeModified = prevPackage.getFactory().getModel();
        return new Pair<>(diff, modelToBeModified);
    }

    /**
     * Build a model.
     *
     * @param file program whose model needs to be built
     * @return located node in the prev file model
     * @throws FileNotFoundException Exception raised via {@link SpoonResourceHelper}
     */
    public static CtModel buildModel(File file) throws FileNotFoundException {
        final SpoonResource resource = SpoonResourceHelper.createResource(file);
        final Launcher launcher = new Launcher();
        Environment env = launcher.getEnvironment();
        env.setCommentEnabled(false); // TODO enable comments

        env.setPrettyPrinterCreator(
                () -> {
                    DefaultJavaPrettyPrinter printer = new DefaultJavaPrettyPrinter(env);
                    printer.setIgnoreImplicit(false); // required to NOT print e.g. implicit "this"

                    return printer;
                });
        launcher.addInputResource(resource);
        return launcher.buildModel();
    }

    private static List<CtCompilationUnit> getAllCompilationUnits(CtElement element) {
        return List.copyOf(
                element.getParent(CtModule.class).getFactory().CompilationUnit().getMap().values());
    }

    /**
     * Returns the only compilation unit built while the model is created.
     *
     * @param element element whose corresponding compilation unit is needed
     * @return the compilation unit corresponding to the only file with which the model was built
     */
    public static CtCompilationUnit getTheOnlyCompilationUnit(CtElement element) {
        List<CtCompilationUnit> compilationUnits = getAllCompilationUnits(element);
        if (compilationUnits.size() != 1) {
            throw new IllegalArgumentException(
                    "Model should have exactly 1 compilation unit, but has - "
                            + compilationUnits.size());
        }
        return compilationUnits.get(0);
    }

    /**
     * Pretty-prints the model using the single compilation unit it has.
     *
     * @param model model to be pretty printed
     * @return patched program
     */
    public static String prettyPrintModelWithSingleCompilationUnit(CtModel model) {
        List<CtCompilationUnit> compilationUnits =
                List.copyOf(
                        model.getUnnamedModule().getFactory().CompilationUnit().getMap().values());
        if (compilationUnits.size() != 1) {
            throw new IllegalArgumentException(
                    "Model should have exactly 1 compilation unit, but has - "
                            + compilationUnits.size());
        }
        CtCompilationUnit modelCu = compilationUnits.get(0);
        // Note: Must explicitly create our configured pretty printer, as spoon-9.0.0 has that
        // CompilationUnit.prettyprint() always uses the auto-import pretty-printer, and not
        // our custom configured one.
        PrettyPrinter printer = modelCu.getFactory().getEnvironment().createPrettyPrinter();
        return printer.prettyprint(modelCu);
    }
}
