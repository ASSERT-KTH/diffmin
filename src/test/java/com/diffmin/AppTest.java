package com.diffmin;

import com.diffmin.App;

import gumtree.spoon.AstComparator;
import org.junit.Test;
import spoon.reflect.declaration.CtElement;
import gumtree.spoon.diff.operations.Operation;


import java.io.File;
import java.util.List;

/**
 * Unit test for simple App.
 */
public class AppTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void prevFileShouldBecomeNewFile() throws Exception {
        File f1 = new File("src/test/resources/action1.java");
        File f2 = new File("src/test/resources/action2.java");
        CtElement c2 = new AstComparator().getCtType(f2);
        List<Operation> operations = App.getOperations(f1, f2);
        CtElement patchedCtElement = App.patch(f1, operations);
        assert patchedCtElement.prettyprint().equals(c2.prettyprint());
    }
}
