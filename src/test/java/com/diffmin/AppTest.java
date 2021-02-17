package com.diffmin;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;

import gumtree.spoon.AstComparator;
import org.junit.jupiter.params.provider.MethodSource;
import spoon.reflect.declaration.CtElement;
import gumtree.spoon.diff.operations.Operation;

import java.io.File;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest
{
    @DisplayName("Should patch old file with Update operations")
    @ParameterizedTest(name="{0}")
    @MethodSource("resourceProvider")
    public void shouldUpdate( String testMessage, String filePathPrev, String filePathNew) throws Exception {
        File f1 = new File(filePathPrev);
        File f2 = new File(filePathNew);
        CtElement expectedNewElement = new AstComparator().getCtType(f2);
        List<Operation> operations = App.getOperations(f1, f2);
        CtElement patchedCtElement = App.patch(f1, operations);
        assert patchedCtElement.prettyprint().equals(expectedNewElement.prettyprint());
    }

    private static Arguments[] resourceProvider() {
        return new Arguments[]{
            Arguments.of("Should update literal", "src/test/resources/update/string_literal_prev.java", "src/test/resources/update/string_literal_new.java"),
            Arguments.of("Should update invocation", "src/test/resources/update/invocation_prev.java", "src/test/resources/update/invocation_new.java")
        };
    }
}
