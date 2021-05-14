package com.diffmin.patch;

import com.diffmin.SpoonMapping;
import com.diffmin.util.Pair;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Update;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.operations.Operation;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatementList;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;

/** Class for generating patches. */
public class Generation {
    public final Set<CtElement> deletePatches = new HashSet<>();
    public final Set<Pair<CtElement, CtElement>> updatePatches = new HashSet<>();
    public final Set<ImmutableTriple<Integer, CtElement, CtElement>> insertPatches =
            new HashSet<>();

    /** Generates the patches. */
    public void generatePatch(Diff diff) {
        @SuppressWarnings("rawtypes")
        List<Operation> operations = diff.getRootOperations();
        SpoonMapping mapping = SpoonMapping.fromGumTreeMapping(diff.getMappingsComp());
        for (Operation<?> operation : operations) {
            if (operation.getAction() instanceof Delete) {
                deletePatches.add(delete(operation.getSrcNode()));
            } else if (operation.getAction() instanceof Update) {
                updatePatches.add(update(operation.getSrcNode(), operation.getDstNode()));
            } else if (operation.getAction() instanceof Insert) {
                insertPatches.add(insert(operation.getSrcNode(), mapping));
                insert(operation.getSrcNode(), mapping);
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
                        if (srcNodeParent.getValueByRole(srcNode.getRoleInParent())
                                instanceof List) {
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
                                return ((CtInvocation<?>) element.getParent()).getArguments();
                            case TYPE_MEMBER:
                                return ((CtClass<?>) element.getParent()).getTypeMembers();
                            case TYPE_PARAMETER:
                                return ((CtClass<?>) element.getParent())
                                        .getFormalCtTypeParameters();
                            case PARAMETER:
                                return ((CtExecutable<?>) element.getParent()).getParameters();
                            default:
                                throw new UnsupportedOperationException(
                                        "Unsupported role: " + element.getRoleInParent());
                        }
                    }
                };
        int srcNodeIndex = iUtil.getSrcNodeIndex(insertedNode);
        CtElement parentElementInPrevModel = mapping.get(insertedNode.getParent());
        return new ImmutableTriple<>(srcNodeIndex, insertedNode, parentElementInPrevModel);
    }

    private interface InsertionUtil {
        int getSrcNodeIndex(CtElement element);
    }
}
