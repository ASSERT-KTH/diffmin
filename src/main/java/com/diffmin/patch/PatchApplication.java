package com.diffmin.patch;

import static spoon.reflect.path.CtRole.CONTAINED_TYPE;

import com.diffmin.util.Pair;
import com.diffmin.util.SpoonUtil;
import gumtree.spoon.builder.CtVirtualElement;
import gumtree.spoon.builder.CtWrapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtAbstractSwitch;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtFormalTypeDeclarer;
import spoon.reflect.declaration.CtModifiable;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtType;
import spoon.reflect.declaration.CtTypeMember;
import spoon.reflect.declaration.CtTypeParameter;
import spoon.reflect.declaration.ModifierKind;
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

        // update type in compilation unit
        if (prevNode.getRoleInParent() == CONTAINED_TYPE) {
            CtCompilationUnit inWhichCompilationUnit =
                    SpoonUtil.getTheOnlyCompilationUnit(prevNode.getParent());

            List<CtType<?>> types = new ArrayList<>(inWhichCompilationUnit.getDeclaredTypes());
            int indexOfOldType =
                    inWhichCompilationUnit.getDeclaredTypes().indexOf((CtType<?>) prevNode);
            types.set(indexOfOldType, (CtType<?>) newNode);
            inWhichCompilationUnit.setDeclaredTypes(types);
        }

        // update type in model
        prevNode.replace(newNode);
    }

    /** Apply the insert patch. */
    @SuppressWarnings("unchecked")
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
                ((CtAbstractInvocation<?>) inWhichElement)
                        .addArgumentAt(where, (CtExpression<?>) toBeInserted);
                break;
            case TYPE_MEMBER:
                ((CtType<?>) inWhichElement).addTypeMemberAt(where, (CtTypeMember) toBeInserted);
                break;
            case TYPE_PARAMETER:
                ((CtFormalTypeDeclarer) inWhichElement)
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
                CtCompilationUnit inWhichCompilationUnit =
                        SpoonUtil.getTheOnlyCompilationUnit(inWhichElement);
                List<CtType<?>> types = new ArrayList<>(inWhichCompilationUnit.getDeclaredTypes());
                types.add(where, (CtType<?>) toBeInserted);
                inWhichCompilationUnit.setDeclaredTypes(types);
                break;
            case MODIFIER:
                if (toBeInserted instanceof CtVirtualElement) {
                    Set<ModifierKind> newModifiers = new HashSet<>();
                    for (Object modifierKind : ((CtVirtualElement) toBeInserted).getChildren()) {
                        newModifiers.add((ModifierKind) modifierKind);
                    }
                    ((CtModifiable) inWhichElement).setModifiers(newModifiers);
                } else {
                    ((CtModifiable) inWhichElement)
                            .addModifier((ModifierKind) ((CtWrapper<?>) toBeInserted).getValue());
                }
                break;
            case CASE:
                ((CtAbstractSwitch<Object>) inWhichElement)
                        .addCaseAt(where, (CtCase<? super Object>) toBeInserted);
                break;
            case EXPRESSION:
                List<CtExpression<Object>> caseExpressions =
                        ((CtCase<Object>) inWhichElement).getCaseExpressions();
                if (caseExpressions.isEmpty()) {
                    ((CtCase<Object>) inWhichElement)
                            .addCaseExpression((CtExpression<Object>) toBeInserted);
                } else {
                    caseExpressions.add(where, (CtExpression<Object>) toBeInserted);
                }
                break;
            case ANNOTATION:
                // it is necessary to copy annotations because it returns an unmodifiable list
                List<CtAnnotation<?>> annotations =
                        new ArrayList<>(inWhichElement.getAnnotations());
                annotations.add(where, (CtAnnotation<?>) toBeInserted);
                inWhichElement.setAnnotations(annotations);
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
