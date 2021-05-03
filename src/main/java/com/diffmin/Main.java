package com.diffmin;

import com.diffmin.util.Pair;
import gumtree.spoon.diff.Diff;
import java.io.File;
import java.io.FileNotFoundException;
import spoon.reflect.CtModel;

/** Main execution of generating and applying patch */
class Main {
    private final App app;

    private final CtModel patchedCtModel;

    /**
     * Runs the patch function and generates a patched model.
     *
     * @param prevFile Previous version of the file
     * @param newFile Modified version of the file
     * @throws FileNotFoundException Exception is raised when path of the file is invalid
     */
    Main(File prevFile, File newFile) throws FileNotFoundException {
        app = new App();
        Pair<Diff, CtModel> diffAndModel = App.computeDiff(prevFile, newFile);
        app.generatePatch(diffAndModel.getFirst());
        app.applyPatch();
        patchedCtModel = diffAndModel.getSecond();
    }

    /** Returns the patched model. */
    public CtModel getModel() {
        return patchedCtModel;
    }

    /** Pretty-prints the patched model. */
    public String displayModel() {
        return app.displayModifiedModel(patchedCtModel);
    }
}
