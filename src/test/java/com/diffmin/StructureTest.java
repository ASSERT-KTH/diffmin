package com.diffmin;

import static org.junit.jupiter.api.Assertions.*;

import com.diffmin.util.SpoonUtil;
import java.io.File;
import java.util.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtType;

/** Unit tests for verifying structure. */
class StructureTest {
    @ParameterizedTest
    @ArgumentsSource(ResourceProvider.PureUpdatePatches.class)
    void should_apply_pure_update_patches(ResourceProvider.TestResources sources) throws Exception {
        runTests(sources);
    }

    @ParameterizedTest
    @ArgumentsSource(ResourceProvider.PureDeletePatches.class)
    void should_apply_pure_delete_patches(ResourceProvider.TestResources sources) throws Exception {
        runTests(sources);
    }

    @ParameterizedTest
    @ArgumentsSource(ResourceProvider.PureInsertPatches.class)
    void should_apply_pure_insert_patches(ResourceProvider.TestResources sources) throws Exception {
        runTests(sources);
    }

    @ParameterizedTest
    @ArgumentsSource(ResourceProvider.PureMovePatches.class)
    void should_apply_pure_move_patches(ResourceProvider.TestResources sources) throws Exception {
        runTests(sources);
    }

    @ParameterizedTest
    @ArgumentsSource(ResourceProvider.MixOperationPatches.class)
    void should_apply_mix_operation_patches(ResourceProvider.TestResources sources)
            throws Exception {
        runTests(sources);
    }

    private static void runTests(ResourceProvider.TestResources sources) throws Exception {
        File f1 = sources.prevPath.toFile();
        File f2 = sources.newPath.toFile();

        CtModel patchedCtModel = Main.patchAndGenerateModel(f1, f2);
        CtModel expectedModel = SpoonUtil.buildModel(sources.newPath.toFile());
        Optional<CtType<?>> firstType = expectedModel.getAllTypes().stream().findFirst();
        if (firstType.isEmpty()) {
            assertTrue(
                    patchedCtModel.getAllTypes().stream().findFirst().isEmpty(),
                    "Patched prev file is not empty");
        } else {
            CtType<?> retrievedFirstType = firstType.get();
            CtCompilationUnit cu =
                    retrievedFirstType
                            .getFactory()
                            .CompilationUnit()
                            .getOrCreate(retrievedFirstType);
            String patchedProgram =
                    SpoonUtil.prettyPrintModelWithSingleCompilationUnit(patchedCtModel);
            assertEquals(cu.prettyprint(), patchedProgram, "Prev file was not patched correctly");
        }
    }
}
