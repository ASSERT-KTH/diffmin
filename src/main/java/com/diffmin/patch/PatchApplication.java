package com.diffmin.patch;

import com.diffmin.util.Pair;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtTypeReference;

/** Class for applying patches. */
public class PatchApplication {

    /** Override constructor to prevent instantiating of this class (RSPEC-1118). */
    private PatchApplication() {
        throw new IllegalStateException("Utility classes should not be instantiated");
    }

    /** Apply all the patches generated. */
    public static void applyPatch(
            List<CtElement> deletePatches,
            List<Pair<CtElement, CtElement>> updatePatches,
            List<ImmutableTriple<Integer, CtElement, CtElement>> insertPatches,
            List<Pair<CtElement, ImmutableTriple<Integer, CtElement, CtElement>>> movePatches) {
        deletePatches.forEach(PatchApplication::performDeletion);
        updatePatches.forEach(PatchApplication::performUpdating);
        insertPatches.forEach(PatchApplication::performInsertion);
        movePatches.forEach(PatchApplication::performMovement);
    }

    /** Apply the delete patch. */
    private static void performDeletion(CtElement toBeDeleted) {
        toBeDeleted.delete();
    }

    /** Apply the update patch. */
    private static void performUpdating(Pair<CtElement, CtElement> updatePatch) {
        CtElement prevNode = updatePatch.getFirst();
        CtElement newNode = updatePatch.getSecond();
        prevNode.replace(newNode);
    }

    /** Apply the insert patch. */
    private static void performInsertion(
            ImmutableTriple<Integer, CtElement, CtElement> insertPatch) {
        int where = insertPatch.left;
        CtElement toBeInserted = insertPatch.middle;
        CtElement inWhichElement = insertPatch.right;

        switch (toBeInserted.getRoleInParent()) {
            case STATEMENT:
                ((CtStatementList) inWhichElement)
                        .addStatement(where, (CtStatement) toBeInserted.clone());
                break;
            case ARGUMENT:
                ((CtInvocation<?>) inWhichElement)
                        .addArgumentAt(where, (CtExpression<?>) toBeInserted);
                break;
            case TYPE_MEMBER:
                ((CtClass<?>) inWhichElement).addTypeMemberAt(where, (CtTypeMember) toBeInserted);
                break;
            case TYPE_PARAMETER:
                ((CtClass<?>) inWhichElement)
                        .addFormalCtTypeParameterAt(where, (CtTypeParameter) toBeInserted);
                break;
            case PARAMETER:
                ((CtExecutable<?>) inWhichElement)
                        .addParameterAt(where, (CtParameter<?>) toBeInserted);
                break;
            case THROWN:
                Set<CtTypeReference<? extends Throwable>> thrownTypesCopy =
                        new HashSet<>(
                                ((CtExecutable<?>) toBeInserted.getParent()).getThrownTypes());
                ((CtExecutable<?>) inWhichElement).setThrownTypes(thrownTypesCopy);
                break;
            case CONTAINED_TYPE:
                // Inserting into CtPackage
                ((CtPackage) inWhichElement).addType((CtType<?>) toBeInserted.clone());

                // Inserting into CtCompilationUnit
                List<CtCompilationUnit> compilationUnits =
                        List.copyOf(
                                ((CtPackage) inWhichElement)
                                        .getDeclaringModule()
                                        .getFactory()
                                        .CompilationUnit()
                                        .getMap()
                                        .values());
                if (compilationUnits.size() != 1) {
                    throw new IllegalArgumentException(
                            "Model should have exactly 1 compilation unit, but has - "
                                    + compilationUnits.size());
                }
                CtCompilationUnit cu = compilationUnits.get(0);
                List<CtType<?>> types = new ArrayList<>(cu.getDeclaredTypes());
                types.add(where, (CtType<?>) toBeInserted);
                cu.setDeclaredTypes(types);
                break;
            default:
                inWhichElement.setValueByRole(toBeInserted.getRoleInParent(), toBeInserted);
                break;
        }
    }

    private static void performMovement(
            Pair<CtElement, ImmutableTriple<Integer, CtElement, CtElement>> movePatch) {
        CtElement toBeDeleted = movePatch.getFirst();
        ImmutableTriple<Integer, CtElement, CtElement> toBeInserted = movePatch.getSecond();

        performDeletion(toBeDeleted);
        performInsertion(toBeInserted);
    }
}
