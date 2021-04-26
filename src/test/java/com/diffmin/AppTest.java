package com.diffmin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import com.diffmin.util.Pair;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.DefaultJavaPrettyPrinter;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static final Path RESOURCES_BASE_DIR = Paths.get("src/test/resources");
    public static final Path PURE_DELETE_PATCHES = RESOURCES_BASE_DIR.resolve("delete");
    public static final Path PURE_UPDATE_PATCHES = RESOURCES_BASE_DIR.resolve("update");
    public static final Path PURE_INSERT_PATCHES = RESOURCES_BASE_DIR.resolve("insert");
    public static final Path DELETE_UPDATE_PATCHES = RESOURCES_BASE_DIR.resolve("delete+update");

    private static Stream<? extends Arguments> getArgumentSourceStream(
            File testDir, Function<File, TestResources> sourceGetter) {
        return Arrays.stream(testDir.listFiles())
                .filter(File::isDirectory)
                .map(sourceGetter)
                .map(Arguments::of);
    }

    /**
     * Class to provide test resources.
     */
    public static class TestResources {
        public String parent;
        public Path prevPath;
        public Path newPath; // stylised new

        /**
         * Constructor of {@link TestResources}.
         *
         * @param prevPath path of the previous version of a file
         * @param newPath path of the new version of a file
         * @param parent name of the directory containing the two files
         */
        TestResources(Path prevPath, Path newPath, String parent) {
            this.prevPath = prevPath;
            this.newPath = newPath;
            this.parent = parent;
        }

        /**
         * Resolve files inside a directory.
         *
         * @param testDir Directory containing test files
         * @return instance of {@link TestResources}
         */
        public static TestResources fromTestDirectory(File testDir) {
            Path path = testDir.toPath();
            String parent = testDir.getName();
            return new TestResources(
                    path.resolve("prev.java"),
                    path.resolve("new.java"),
                    parent
            );
        }

        @Override
        public String toString() {
            return parent;
        }
    }

    /** Provides test sources for scenarios where only update patches are applied. */
    public static class PureUpdatePatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    PURE_UPDATE_PATCHES.toFile(),
                    TestResources::fromTestDirectory
            );
        }
    }

    /** Provides test sources for scenarios where only delete patches are applied. */
    public static class PureDeletePatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    PURE_DELETE_PATCHES.toFile(),
                    TestResources::fromTestDirectory
            );
        }
    }

    /** Provides test sources for scenarios where only delete patches are applied. */
    public static class DeleteUpdatePatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    DELETE_UPDATE_PATCHES.toFile(),
                    TestResources::fromTestDirectory
            );
        }
    }

    /** Provides test sources for scenarios where only insert patches are applied. */
    public static class PureInsertPatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    PURE_INSERT_PATCHES.toFile(),
                    TestResources::fromTestDirectory
            );
        }
    }

    @ParameterizedTest
    @ArgumentsSource(PureUpdatePatches.class)
    void should_apply_pure_update_patches(TestResources sources) throws Exception {
        runTests(sources);
    }

    @ParameterizedTest
    @ArgumentsSource(PureDeletePatches.class)
    void should_apply_pure_delete_patches(TestResources sources) throws Exception {
        runTests(sources);
    }

    @ParameterizedTest
    @ArgumentsSource(DeleteUpdatePatches.class)
    void should_apply_delete_update_patches(TestResources sources) throws Exception {
        runTests(sources);
    }

    @ParameterizedTest
    @ArgumentsSource(PureInsertPatches.class)
    void should_apply_pure_insert_patches(TestResources sources) throws Exception {
        runTests(sources);
    }

    private static void runTests(TestResources sources) throws Exception {
        File f1 = sources.prevPath.toFile();
        File f2 = sources.newPath.toFile();

        App app = new App();

        Pair<Diff, CtModel> diffAndModel = App.computeDiff(f1, f2);
        app.generatePatch(diffAndModel.getFirst());
        app.applyPatch();
        CtModel patchedCtModel = diffAndModel.getSecond();

        CtModel expectedModel = App.buildModel(sources.newPath.toFile());
        Optional<CtType<?>> firstType = expectedModel.getAllTypes().stream().findFirst();

        if (firstType.isEmpty()) {
            assertTrue(
                    patchedCtModel.getAllTypes().stream().findFirst().isEmpty(),
                    "Patched prev file is not empty"
            );
        }
        else {
            CtType<?> retrievedFirstType = firstType.get();
            CtCompilationUnit cu = retrievedFirstType
                    .getFactory().CompilationUnit().getOrCreate(retrievedFirstType);
            String patchedProgram = app.displayModifiedModel(patchedCtModel);
            assertEquals(
                    cu.prettyprint(),
                    patchedProgram,
                    "Prev file was not patched correctly"
            );
        }
    }
}
