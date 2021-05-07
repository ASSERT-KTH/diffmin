package com.diffmin;

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
    public static CtModel patchAndGenerateModel(File prevFile, File newFile)
            throws FileNotFoundException {
        App app = new App();
        Pair<Diff, CtModel> diffAndModel = App.computeDiff(prevFile, newFile);
        app.generatePatch(diffAndModel.getFirst());
        app.applyPatch();
        return diffAndModel.getSecond();
    }

    /**
     * Runs the patch function and dumps the output in the terminal.
     *
     * @param args Arguments passed via command line
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: DiffSpoon <file_1>  <file_2>");
            System.exit(1);
        }
        try {
            App app = new App();
            CtModel patchedCtModel =
                    Main.patchAndGenerateModel(new File(args[0]), new File(args[1]));
            System.out.println(app.displayModifiedModel(patchedCtModel));
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
