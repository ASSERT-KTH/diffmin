package com.diffmin;

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
     * @param filePathLeft File path of previous version
     * @param filePathRight File path of modified version
     * @throws Exception Exception raised via {@link AstComparator}, {@link App} and,
     * {@link AssertionError}
     */
    @DisplayName("Should patch old file with operations")
    @ParameterizedTest(name = "{0}")
    @MethodSource("resourceProvider")
    public void shouldPatch(
            String testMessage, String filePathLeft, String filePathRight
    ) throws Exception {
        File f1 = new File(filePathLeft);
        File f2 = new File(filePathRight);
        CtElement expectedNewElement = new AstComparator().getCtType(f2);
        List<Operation> operations = App.getOperations(f1, f2);
        CtModel patchedCtModel = App.patch(filePathLeft, operations);
        if (patchedCtModel.getRootPackage().isEmpty()) {
            assert expectedNewElement == null;
        }
        else {
            CtElement patchedCtElement = patchedCtModel.getRootPackage().getDirectChildren().get(0);
            assert expectedNewElement.prettyprint().equals(patchedCtElement.prettyprint());
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
            )
        };
    }
}
