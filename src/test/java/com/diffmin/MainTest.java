package com.diffmin;

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.security.Permission;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit test to verify Main method's functioning */
class MainTest {
    /** Overrides the default security manager before starting execution of test cases. */
    @BeforeAll
    static void beforeAll() {
        System.setSecurityManager(new DoNotExitJVM());
    }

    /** Restores the default security manager after executing all test cases. */
    @AfterAll
    static void afterAll() {
        System.setSecurityManager(null);
    }

    /** Custom exception to be thrown whenever {@link System#exit(int)} is invoked. */
    @SuppressWarnings("serial")
    private static class ExitException extends SecurityException {
        public final int status;

        public ExitException(int status) {
            this.status = status;
        }
    }

    /** Override {@link SecurityManager} to prevent exiting JVM */
    private static class DoNotExitJVM extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {}

        @Override
        public void checkPermission(Permission perm, Object context) {}

        /**
         * Throws an {@link ExitException} instead of exiting the JVM.
         *
         * @param status Exit code of the invocation
         */
        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException(status);
        }
    }

    @Test
    @DisplayName("should exit with code 1 when incorrect number of parameters are supplied")
    void should_exit_1_if_wrong_number_of_parameters_are_supplied() {
        ExitException exit = assertThrows(ExitException.class, () -> Main.main(new String[] {}));
        assertEquals(1, exit.status);
    }

    @Test
    @DisplayName("should throw FileNotFoundException if incorrect path is supplied")
    void should_throw_FileNotFoundException_if_path_is_incorrect() {
        String prevFile = "wrong/path/to/prevFile";
        String newFile = "wrong/path/to/newFile";
        assertThrows(
                FileNotFoundException.class, () -> Main.main(new String[] {prevFile, newFile}));
    }

    @Test
    @DisplayName("should exit with code 0 when two correct paths are supplied")
    void should_exit_0_when_2_correct_paths_are_supplied() {
        String prevFile = "src/test/resources/delete/literal/PREV_DeleteLiteral.java";
        String newFile = "src/test/resources/delete/literal/NEW_DeleteLiteral.java";

        ExitException exit =
                assertThrows(
                        ExitException.class, () -> Main.main(new String[] {prevFile, newFile}));
        assertEquals(0, exit.status);
    }
}
