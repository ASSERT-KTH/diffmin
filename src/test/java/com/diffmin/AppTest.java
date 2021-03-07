package com.diffmin;

import gumtree.spoon.AstComparator;
import gumtree.spoon.diff.operations.Operation;
import java.io.File;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import spoon.reflect.declaration.CtElement;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Tests for verifying if patch works correctly.
     *
     * @param testMessage  Test message to be displayed
     * @param filePathPrev File path of previous version
     * @param filePathNew  File path of modified version
     * @throws Exception Exception raised via {@link AstComparator}, {@link App} and,
     * {@link AssertionError}
     */
    @DisplayName("Should patch old file with operations")
    @ParameterizedTest(name = "{0}")
    @MethodSource("resourceProvider")
    public void shouldPatch(
            String testMessage, String filePathPrev, String filePathNew
    ) throws Exception {
        File f1 = new File(filePathPrev);
        File f2 = new File(filePathNew);
        CtElement expectedNewElement = new AstComparator().getCtType(f2);
        List<Operation> operations = App.getOperations(f1, f2);
        CtElement patchedCtElement = App.patch(f1, operations);
        assert patchedCtElement.prettyprint().equals(expectedNewElement.prettyprint());
    }

    private static Arguments[] resourceProvider() {
        return new Arguments[]{
            // Pure update patches
            Arguments.of(
                "Should update literal",
                "src/test/resources/update/literal/prev.java",
                "src/test/resources/update/literal/new.java"
            ),
            Arguments.of(
                "Should update invocation",
                "src/test/resources/update/invocation/prev.java",
                "src/test/resources/update/invocation/new.java"
            ),
            Arguments.of(
                "Should update literal and invocation",
                "src/test/resources/update/literal+invocation/prev.java",
                "src/test/resources/update/literal+invocation/new.java"
            ),
            Arguments.of(
                "Should update type reference",
                "src/test/resources/update/typeref/prev.java",
                "src/test/resources/update/typeref/new.java"
            ),
            // Pure delete patches
            Arguments.of(
                "Should delete literal",
                "src/test/resources/delete/literal/prev.java",
                "src/test/resources/delete/literal/new.java"
            ),
            // Delete + Update patches
            Arguments.of(
                "Should update literal, parameter, and delete while",
                "src/test/resources/delete+update/literal+while+parameter/prev.java",
                "src/test/resources/delete+update/literal+while+parameter/new.java"
            ),
            Arguments.of(
                "Should update type reference",
                "src/test/resources/update/typeref/prev.java",
                "src/test/resources/update/typeref/new.java"
            ),
            // Pure insert patches
            Arguments.of(
                "Should insert local variable",
                "src/test/resources/insert/local_variable/prev.java",
                "src/test/resources/insert/local_variable/new.java"
            ),
            Arguments.of(
                "Should insert class",
                "src/test/resources/insert/class/prev.java",
                "src/test/resources/insert/class/new.java"
            ),
            Arguments.of(
                "Should insert loop and update local variable",
                "src/test/resources/insert/for_loop+local_variable/prev.java",
                "src/test/resources/insert/for_loop+local_variable/new.java"
            ),
            // Insert + Update patches
            Arguments.of(
                "Should update class and insert invocation",
                "src/test/resources/insert+update/invocation_class/prev.java",
                "src/test/resources/insert+update/invocation_class/new.java"
            ),
        };
    }
}
