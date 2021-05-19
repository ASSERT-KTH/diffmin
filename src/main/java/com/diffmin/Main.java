package com.diffmin;

import com.diffmin.patch.PatchApplication;
import com.diffmin.patch.PatchGeneration;
import com.diffmin.util.Pair;
import gumtree.spoon.diff.Diff;
import java.io.File;
import java.io.FileNotFoundException;
import spoon.reflect.CtModel;

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
        PatchGeneration patchGeneration = new PatchGeneration();
        patchGeneration.generatePatch(diffAndModel.getFirst());

        // Apply patches
        PatchApplication.applyPatch(
                patchGeneration.getDeletePatches(),
                patchGeneration.getUpdatePatches(),
                patchGeneration.getInsertPatches(),
                patchGeneration.getMovePatches());

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
