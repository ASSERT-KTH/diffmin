package com.diffmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Tests for verifying if patch works correctly.
     *
     * @param testMessage  Test message to be displayed
     * @param prevFilePath File path of previous version
     * @param nextFilePath File path of modified version
     * @throws Exception Exception raised via {@link AstComparator}, {@link App} and,
     * {@link AssertionError}
     */
    @DisplayName("Should patch old file with operations")
    @ParameterizedTest(name = "{0}")
    @MethodSource("resourceProvider")
    public void shouldPatch(
            String testMessage, String prevFilePath, String nextFilePath
    ) throws Exception {
        File f1 = new File(prevFilePath);
        File f2 = new File(nextFilePath);
        CtElement expectedNewElement = new AstComparator().getCtType(f2);
        App app = new App(prevFilePath);
        List<Operation> operations = app.getOperations(f1, f2);
        app.generatePatch(operations);
        app.applyPatch();
        CtModel patchedCtModel = app.modelToBeModified;
        if (patchedCtModel.getRootPackage().isEmpty()) {
            assertNull(expectedNewElement, "Patched prev file is not empty");
        }
        else {
            String patchedProgram = app.displayModifiedModel(patchedCtModel);
            assertEquals(
                    expectedNewElement.prettyprint(),
                    patchedProgram,
                    "Prev file was not patched correctly"
            );
        }
    }

    private static Arguments[] resourceProvider() {
        return new Arguments[]{
            // Pure delete patches
            Arguments.of(
                "Should delete literal",
                "src/test/resources/delete/literal/left.java",
                "src/test/resources/delete/literal/right.java"
            ),
            Arguments.of(
                "Should delete a specific literal",
                "src/test/resources/delete/specific_literal/left.java",
                "src/test/resources/delete/specific_literal/right.java"
            ),
            Arguments.of(
                "Should delete an entire program",
                "src/test/resources/delete/entire_file/left.java",
                "src/test/resources/delete/entire_file/right.java"
            ),
            // Pure update patches
            Arguments.of(
                "Should update invocation",
                "src/test/resources/update/invocation/prev.java",
                "src/test/resources/update/invocation/new.java"
            ),
            Arguments.of(
                "Should update literal",
                "src/test/resources/update/literal/prev.java",
                "src/test/resources/update/literal/new.java"
            ),
            Arguments.of(
                "Should update literal and invocation",
                "src/test/resources/update/literal/prev.java",
                "src/test/resources/update/literal/new.java"
            ),
            Arguments.of(
                "Should update type reference",
                "src/test/resources/update/typeref/prev.java",
                "src/test/resources/update/typeref/new.java"
            )
        };
    }
}
