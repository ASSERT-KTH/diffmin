package com.diffmin;

import static org.junit.jupiter.api.Assertions.*;

import com.diffmin.util.SpoonUtil;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtType;
import spoon.reflect.visitor.CtScanner;

/** Unit test for simple App. */
public class AppTest {
    public static final Path RESOURCES_BASE_DIR = Paths.get("src/test/resources");

    public static final Path PURE_DELETE_PATCHES = RESOURCES_BASE_DIR.resolve("delete");

    public static final Path PURE_UPDATE_PATCHES = RESOURCES_BASE_DIR.resolve("update");

    public static final Path PURE_INSERT_PATCHES = RESOURCES_BASE_DIR.resolve("insert");

    public static final Path PURE_MOVE_PATCHES = RESOURCES_BASE_DIR.resolve("move");

    public static final Path DELETE_INSERT_PATCHES = RESOURCES_BASE_DIR.resolve("delete+insert");

    public static final Path DELETE_UPDATE_PATCHES = RESOURCES_BASE_DIR.resolve("delete+update");

    private static final String PREV_PREFIX = "PREV";

    private static final String NEW_PREFIX = "NEW";

    private static final String INSERTED_PATHS = "inserted_paths";

    private static Stream<? extends Arguments> getArgumentSourceStream(
            File testDir, Function<File, TestResources> sourceGetter) {
        return Arrays.stream(testDir.listFiles())
                .filter(File::isDirectory)
                .map(sourceGetter)
                .map(Arguments::of);
    }

    /** Class to provide test resources. */
    public static class TestResources {
        public String parent;

        public Path prevPath;

        public Path newPath; // stylised new

        public Path insertedPaths;

        /**
         * Constructor of {@link TestResources}.
         *
         * @param prevPath path of the previous version of a file
         * @param newPath path of the new version of a file
         * @param parent name of the directory containing the two files
         */
        TestResources(Path prevPath, Path newPath, String parent, Path insertedPaths) {
            this.prevPath = prevPath;
            this.newPath = newPath;
            this.parent = parent;
            this.insertedPaths = insertedPaths;
        }

        /**
         * Resolve files inside a directory.
         *
         * @param testDir Directory containing test files
         * @return instance of {@link TestResources}
         */
        public static TestResources fromTestDirectory(File testDir) {
            String parent = testDir.getName();
            Path prevPath = getFilepathByPrefix(testDir, PREV_PREFIX);
            Path newPath = getFilepathByPrefix(testDir, NEW_PREFIX);
            Path insertedPaths = getFilepathByPrefix(testDir, INSERTED_PATHS);
            return new TestResources(prevPath, newPath, parent, insertedPaths);
        }

        /**
         * Returns test resource with the matching prefix.
         *
         * @param dir Directory where the test resources are located
         * @param prefix Prefix of the test resource
         * @return {@link Path} to the test resource
         */
        private static Path getFilepathByPrefix(File dir, String prefix) {
            return Arrays.stream(dir.listFiles())
                    .filter(f -> f.getName().startsWith(prefix))
                    .findFirst()
                    .map(File::toPath)
                    .orElseThrow(
                            () ->
                                    new RuntimeException(
                                            String.format(
                                                    "Expected file with prefix '%s' in directory '%s'",
                                                    prefix, dir)));
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
                    PURE_UPDATE_PATCHES.toFile(), TestResources::fromTestDirectory);
        }
    }

    /** Provides test sources for scenarios where only delete patches are applied. */
    public static class PureDeletePatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    PURE_DELETE_PATCHES.toFile(), TestResources::fromTestDirectory);
        }
    }

    /** Provides test sources for scenarios where delete and insert patches are applied. */
    public static class DeleteInsertPatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    DELETE_INSERT_PATCHES.toFile(), TestResources::fromTestDirectory);
        }
    }

    /** Provides test sources for scenarios where delete and update patches are applied. */
    public static class DeleteUpdatePatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    DELETE_UPDATE_PATCHES.toFile(), TestResources::fromTestDirectory);
        }
    }

    /** Provides test sources for scenarios where only insert patches are applied. */
    public static class PureInsertPatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    PURE_INSERT_PATCHES.toFile(), TestResources::fromTestDirectory);
        }
    }

    /** Provides test sources for scenarios where only move patches are applied. */
    public static class PureMovePatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    PURE_MOVE_PATCHES.toFile(), TestResources::fromTestDirectory);
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
    @ArgumentsSource(DeleteInsertPatches.class)
    void should_apply_delete_insert_patches(TestResources sources) throws Exception {
        runTests(sources);
    }

    @ParameterizedTest
    @ArgumentsSource(DeleteUpdatePatches.class)
    void should_apply_delete_update_patches(TestResources sources) throws Exception {
        runTests(sources);
    }

    @ParameterizedTest
    @ArgumentsSource(AppTest.PureInsertPatches.class)
    void should_apply_pure_insert_patches(TestResources sources) throws Exception {
        runTests(sources);
    }

    @ParameterizedTest
    @ArgumentsSource(AppTest.PureMovePatches.class)
    void should_apply_pure_move_patches(TestResources sources) throws Exception {
        runTests(sources);
    }

    private static void runTests(TestResources sources) throws Exception {
        File f1 = sources.prevPath.toFile();
        File f2 = sources.newPath.toFile();

        // check the structure
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
            String patchedProgram = SpoonUtil.displayModifiedModel(patchedCtModel);
            assertEquals(cu.prettyprint(), patchedProgram, "Prev file was not patched correctly");

            // check the root origination of each element
            Set<String> insertedLines = new HashSet<>(Files.readAllLines(sources.insertedPaths));

            CtScanner scanner =
                    new CtScanner() {
                        @Override
                        public void scan(CtElement element) {
                            if (element != null
                                    && !element.isImplicit()
                                    && element.getPosition().isValidPosition()) {
                                String pathString = element.getPath().toString();
                                // Check if element is children of an inserted element
                                if (insertedLines.stream()
                                        .anyMatch(
                                                line ->
                                                        !(line.equals(pathString))
                                                                && pathString.startsWith(line))) {
                                    return;
                                }

                                // Check if any of the path in metadata file match in patched
                                // program
                                if (insertedLines.contains(pathString)) {
                                    assertTrue(
                                            element.getPosition()
                                                    .getFile()
                                                    .getName()
                                                    .startsWith(NEW_PREFIX),
                                            "Element should originate from new file but does not");
                                }
                                // Case when there is no entry of the path of the element in the
                                // metadata file
                                else {
                                    assertTrue(
                                            element.getPosition()
                                                    .getFile()
                                                    .getName()
                                                    .startsWith(PREV_PREFIX),
                                            "Element should originate from prev file but does not");
                                }
                            }
                            super.scan(element);
                        }
                    };
            scanner.scan(patchedCtModel.getRootPackage());
        }
    }
}
