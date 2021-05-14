package com.diffmin;

import com.diffmin.patch.Application;
import com.diffmin.patch.Generation;
import com.diffmin.util.Pair;
import gumtree.spoon.diff.Diff;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;

/** Main execution of generating and applying patch */
class Main {

    /**
     * Generates patches and apply them to the previous model.
     *
     * @param prevFile Previous version of the file
     * @param newFile Modified version of the file
     * @throws FileNotFoundException Exception is raised when path of either file is invalid
     */
    static CtModel patchAndGenerateModel(File prevFile, File newFile) throws FileNotFoundException {
        Pair<Diff, CtModel> diffAndModel = App.computeDiff(prevFile, newFile);

        // Generate patches
        Generation patchGenerationFactory = new Generation();
        patchGenerationFactory.generatePatch(diffAndModel.getFirst());
        Set<CtElement> deletePatches = patchGenerationFactory.deletePatches;
        Set<Pair<CtElement, CtElement>> updatePatches = patchGenerationFactory.updatePatches;
        Set<ImmutableTriple<Integer, CtElement, CtElement>> insertPatches =
                patchGenerationFactory.insertPatches;

        // Apply patches
        Application patchApplicationFactory =
                new Application(deletePatches, updatePatches, insertPatches);
        patchApplicationFactory.applyPatch();

        // Modified model
        return diffAndModel.getSecond();
    }

    /**
     * Runs the patch function and dumps the output in the terminal.
     *
     * @param args Arguments passed via command line
     */
    public static void main(String[] args) throws FileNotFoundException {
        if (args.length != 2) {
            System.out.println("Usage: DiffSpoon <file_1>  <file_2>");
            System.exit(1);
        }
        CtModel patchedCtModel = Main.patchAndGenerateModel(new File(args[0]), new File(args[1]));
        System.out.println(App.displayModifiedModel(patchedCtModel));
        System.exit(0);
    }
}
