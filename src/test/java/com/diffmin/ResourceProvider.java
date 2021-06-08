package com.diffmin;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

class ResourceProvider {
    private static final Path RESOURCES_BASE_DIR = Paths.get("src/test/resources");
    private static final Path PURE_DELETE_PATCHES = RESOURCES_BASE_DIR.resolve("delete");
    private static final Path PURE_UPDATE_PATCHES = RESOURCES_BASE_DIR.resolve("update");
    private static final Path PURE_INSERT_PATCHES = RESOURCES_BASE_DIR.resolve("insert");
    private static final Path PURE_MOVE_PATCHES = RESOURCES_BASE_DIR.resolve("move");
    private static final Path MIX_OPERATION_PATCHES = RESOURCES_BASE_DIR.resolve("mix-operation");

    static final String PREV_PREFIX = "PREV";
    static final String NEW_PREFIX = "NEW";

    static final String TEST_METADATA = "new_revision_paths";

    private static Stream<? extends Arguments> getArgumentSourceStream(
            File testDir, Function<File, TestResources> sourceGetter) {
        return Arrays.stream(testDir.listFiles())
                .filter(File::isDirectory)
                .map(sourceGetter)
                .map(Arguments::of);
    }

    /** Class to provide test resources. */
    static class TestResources {
        String parent;
        Path prevPath;
        Path newPath; // stylised new
        Path newRevisionPaths;

        private TestResources(Path prevPath, Path newPath, String parent, Path newRevisionPaths) {
            this.prevPath = prevPath;
            this.newPath = newPath;
            this.parent = parent;
            this.newRevisionPaths = newRevisionPaths;
        }

        private static TestResources fromTestDirectory(File testDir) {
            String parent = testDir.getName();
            Path prevPath = getFilepathByPrefix(testDir, PREV_PREFIX);
            Path newPath = getFilepathByPrefix(testDir, NEW_PREFIX);
            Path testMetadata = getFilepathByPrefix(testDir, TEST_METADATA);
            return new TestResources(prevPath, newPath, parent, testMetadata);
        }

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
    static class PureUpdatePatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    PURE_UPDATE_PATCHES.toFile(), TestResources::fromTestDirectory);
        }
    }

    /** Provides test sources for scenarios where only delete patches are applied. */
    static class PureDeletePatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    PURE_DELETE_PATCHES.toFile(), TestResources::fromTestDirectory);
        }
    }

    /** Provides test sources for scenarios where only insert patches are applied. */
    static class PureInsertPatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    PURE_INSERT_PATCHES.toFile(), TestResources::fromTestDirectory);
        }
    }

    /** Provides test sources for scenarios where only move patches are applied. */
    static class PureMovePatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    PURE_MOVE_PATCHES.toFile(), TestResources::fromTestDirectory);
        }
    }

    /** Provides test sources for scenarios where a mix of operations is applied. */
    static class MixOperationPatches implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return getArgumentSourceStream(
                    MIX_OPERATION_PATCHES.toFile(), TestResources::fromTestDirectory);
        }
    }
}
