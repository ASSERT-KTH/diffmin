package com.diffmin.patch;

import com.diffmin.SpoonMapping;
import com.diffmin.util.Pair;
import com.diffmin.util.SpoonUtil;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.Update;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import gumtree.spoon.diff.operations.UpdateOperation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtAbstractSwitch;
import spoon.reflect.code.CtCase;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.CtCompilationUnit;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtFormalTypeDeclarer;
import spoon.reflect.declaration.CtType;
import spoon.reflect.path.CtRole;

/** Class for generating patches. */
public class PatchGeneration {
    private final List<CtElement> deletePatches = new ArrayList<>();
    private final List<Pair<CtElement, CtElement>> updatePatches = new ArrayList<>();
    private final List<ImmutableTriple<Integer, CtElement, CtElement>> insertPatches =
            new ArrayList<>();
    private final List<Pair<CtElement, ImmutableTriple<Integer, CtElement, CtElement>>>
            movePatches = new ArrayList<>();

    /** Returns the delete patches. */
    public List<CtElement> getDeletePatches() {
        return deletePatches;
    }

    /** Returns the update patches. */
    public List<Pair<CtElement, CtElement>> getUpdatePatches() {
        return updatePatches;
    }

    /** Returns the insert patches. */
    public List<ImmutableTriple<Integer, CtElement, CtElement>> getInsertPatches() {
        return insertPatches;
    }

    /** Returns the move patches. */
    public List<Pair<CtElement, ImmutableTriple<Integer, CtElement, CtElement>>> getMovePatches() {
        return movePatches;
    }

    @SuppressWarnings("rawtypes")
    private List<Operation> getRootOperations(Diff diff) {
        List<Operation> operations = diff.getRootOperations();
        List<Operation> rootOperations = new ArrayList<>();

        for (Operation<?> operation : operations) {
            if (isRootOperation(operation, rootOperations)) {
                rootOperations.add(operation);
            }
        }

        return rootOperations;
    }

    @SuppressWarnings("rawtypes")
    private boolean isRootOperation(Operation<?> operation, List<Operation> rootOperations) {
        // assuming that insert, delete, and move root operations are correctly computed by
        // gumtree-spoon-ast-diff
        return !(operation instanceof UpdateOperation)
                // excludes update operations if they are applied on node which is a descendant
                // of another node and it is also being updated
                || rootOperations.stream()
                        .filter(UpdateOperation.class::isInstance)
                        .map(Operation::getSrcNode)
                        .noneMatch(
                                rootOperationSrcNode ->
                                        operation.getSrcNode().hasParent(rootOperationSrcNode));
    }

    /** Generates the patches. */
    public void generatePatch(Diff diff) {
        @SuppressWarnings("rawtypes")
        List<Operation> operations = getRootOperations(diff);
        SpoonMapping mapping = SpoonMapping.fromGumTreeMapping(diff.getMappingsComp());
        for (Operation<?> operation : operations) {
            if (operation.getAction() instanceof Delete) {
                deletePatches.add(delete(operation.getSrcNode()));
            } else if (operation.getAction() instanceof Update) {
                updatePatches.add(update(operation.getSrcNode(), operation.getDstNode()));
            } else if (operation.getAction() instanceof Insert) {
                insertPatches.add(insert(operation.getSrcNode(), mapping));
            } else if (operation.getAction() instanceof Move) {
                movePatches.add(move(operation.getSrcNode(), operation.getDstNode(), mapping));
            }
        }
    }

    private static CtElement delete(CtElement removedNode) {
        return removedNode;
    }

    private static Pair<CtElement, CtElement> update(CtElement srcNode, CtElement dstNode) {
        return new Pair<>(srcNode, dstNode);
    }

    private static ImmutableTriple<Integer, CtElement, CtElement> insert(
            CtElement insertedNode, SpoonMapping mapping) {

        InsertionUtil iUtil =
                new InsertionUtil() {

                    /** Computes the index at which the `insertedNode` has to be inserted. */
                    @Override
                    public int getSrcNodeIndex(CtElement srcNode) {
                        CtElement srcNodeParent = srcNode.getParent();
                        if (srcNodeParent.getValueByRole(srcNode.getRoleInParent()) instanceof List
                                || srcNode.getRoleInParent() == CtRole.CONTAINED_TYPE) {
                            List<? extends CtElement> newCollectionList =
                                    getCollectionElementList(srcNode);
                            return IntStream.range(0, newCollectionList.size())
                                    .filter((i) -> newCollectionList.get(i) == srcNode)
                                    .findFirst()
                                    .getAsInt();
                        }
                        return -1;
                    }

                    /** Returns the corresponding list of elements in parent. */
                    private List<? extends CtElement> getCollectionElementList(CtElement element) {
                        switch (element.getRoleInParent()) {
                            case STATEMENT:
                                return ((CtStatementList) element.getParent()).getStatements();
                            case ARGUMENT:
                                return ((CtAbstractInvocation<?>) element.getParent())
                                        .getArguments();
                            case TYPE_MEMBER:
                                return ((CtType<?>) element.getParent()).getTypeMembers();
                            case TYPE_PARAMETER:
                                return ((CtFormalTypeDeclarer) element.getParent())
                                        .getFormalCtTypeParameters();
                            case PARAMETER:
                                return ((CtExecutable<?>) element.getParent()).getParameters();
                            case CONTAINED_TYPE:
                                CtCompilationUnit cu = SpoonUtil.getTheOnlyCompilationUnit(element);
                                return cu.getDeclaredTypes();
                            case CASE:
                                return ((CtAbstractSwitch<?>) element.getParent()).getCases();
                            case EXPRESSION:
                                return ((CtCase<?>) element.getParent()).getCaseExpressions();
                            case ANNOTATION:
                                return element.getParent().getAnnotations();
                            default:
                                throw new UnsupportedOperationException(
                                        "Unsupported role: " + element.getRoleInParent());
                        }
                    }
                };
        int srcNodeIndex = iUtil.getSrcNodeIndex(insertedNode);
        CtElement parentElementInPrevModel = getMappingOfParent(insertedNode, mapping);
        return new ImmutableTriple<>(srcNodeIndex, insertedNode, parentElementInPrevModel);
    }

    private static CtElement getMappingOfParent(CtElement insertedNode, SpoonMapping mapping) {
        Optional<CtElement> parentElementInPrevModel = mapping.get(insertedNode.getParent());
        if (parentElementInPrevModel.isEmpty()) {
            return insertedNode.getParent();
        }
        return parentElementInPrevModel.get();
    }

    private interface InsertionUtil {
        int getSrcNodeIndex(CtElement element);
    }

    private static Pair<CtElement, ImmutableTriple<Integer, CtElement, CtElement>> move(
            CtElement srcNode, CtElement dstNode, SpoonMapping mapping) {
        CtElement deletedNode = delete(srcNode);
        ImmutableTriple<Integer, CtElement, CtElement> insertedNode = insert(dstNode, mapping);
        return new Pair<>(deletedNode, insertedNode);
    }
}
