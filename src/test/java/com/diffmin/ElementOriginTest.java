package com.diffmin;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.path.CtPathStringBuilder;
import spoon.reflect.path.CtRole;
import spoon.reflect.visitor.CtScanner;

/** Unit tests for verifying element's origin. */
class ElementOriginTest {
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

        List<CtElement> newRevisions =
                Files.readAllLines(sources.newRevisionPaths).stream()
                        .map(pathString -> new CtPathStringBuilder().fromString(pathString))
                        .map(path -> path.evaluateOn(patchedCtModel.getRootPackage()).get(0))
                        .collect(Collectors.toList());
        new ElementSourceFileChecker(newRevisions).scan(patchedCtModel.getRootPackage());
    }

    /** Scanner for checking source file of each element. */
    static class ElementSourceFileChecker extends CtScanner {
        private final Set<CtElement> newRevisions;

        private ElementSourceFileChecker(List<CtElement> newRevisions) {
            this.newRevisions = Collections.newSetFromMap(new IdentityHashMap<>());
            this.newRevisions.addAll(newRevisions);
        }

        @Override
        public void scan(CtElement element) {
            if (!skipAssertionCheck(element)) {
                if (newRevisions.stream().anyMatch(modifiedElement -> modifiedElement == element)
                        || doesElementBelongToModifiedSet(element)) {
                    assertTrue(
                            doesElementBelongToSpecifiedFile(element, ResourceProvider.NEW_PREFIX),
                            "Element should originate from new file but does not");
                } else {
                    assertTrue(
                            doesElementBelongToSpecifiedFile(element, ResourceProvider.PREV_PREFIX),
                            "Element should originate from prev file but does not");
                }
            }
            super.scan(element);
        }

        private static boolean doesElementBelongToSpecifiedFile(
                CtElement element, String filePathPrefix) {
            return element.getPosition().getFile().getName().startsWith(filePathPrefix);
        }

        private static boolean doesElementBelongToModifiedSet(CtElement element) {
            if (element.getRoleInParent() == CtRole.THROWN) {
                return ((CtExecutable<?>) element.getParent())
                        .getThrownTypes().stream()
                                .anyMatch(
                                        thrownType ->
                                                doesElementBelongToSpecifiedFile(
                                                        thrownType, ResourceProvider.NEW_PREFIX));
            }
            return false;
        }

        private boolean isChildOfInsertedPath(CtElement element) {
            return newRevisions.stream().anyMatch(element::hasParent);
        }

        private boolean skipAssertionCheck(CtElement element) {
            if (element == null
                    || element.isImplicit()
                    || !element.getPosition().isValidPosition()) {
                return true;
            }
            return isChildOfInsertedPath(element);
        }
    }
}
