package com.diffmin;

import com.diffmin.util.Pair;
import com.github.gumtreediff.matchers.Mapping;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.tree.TypeSet;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import spoon.reflect.declaration.CtElement;

/**
 * A class for storing matches between tree nodes in two Spoon trees.
 *
 * <p>This class is adapted from the Spork project see <a
 * href="https://github.com/KTH/spork/blob/ba0a33f2bd2f02dc6a50474733be67850f47ae2d/src/main/kotlin/se/kth/spork/spoon/matching/SpoonMapping.kt">se.kth.spork.spoon.matching.SpoonMapping.kt</a>
 */
public class SpoonMapping {
    private final Map<CtElement, CtElement> srcToDst;
    private final Map<CtElement, CtElement> dstToSrc;

    private SpoonMapping() {
        srcToDst = new IdentityHashMap<>();
        dstToSrc = new IdentityHashMap<>();
    }

    /**
     * Create a Spoon mapping from a GumTree mapping. Every GumTree node must have a "spoon_object"
     * metadata object that refers back to a Spoon node. As this mapping does not cover the whole
     * Spoon tree, additional mappings are inferred.
     *
     * <p>TODO verify that the mapping inference is actually correct
     *
     * @param gumtreeMapping A GumTree mapping in which each mapped node has a "spoon_object"
     *     metadata object.
     * @return A SpoonMapping corresponding to the passed GumTree mapping.
     */
    public static SpoonMapping fromGumTreeMapping(MappingStore gumtreeMapping) {
        SpoonMapping mapping = new SpoonMapping();

        for (Mapping m : gumtreeMapping.asSet()) {
            CtElement spoonSrc = getSpoonNode(m.first);
            CtElement spoonDst = getSpoonNode(m.second);
            if (spoonSrc == null || spoonDst == null) {
                if (spoonSrc != spoonDst) { // at least one was non-null
                    throw new IllegalStateException();
                }
                if (m.first.getType()
                        != TypeSet.type("root")) { // -1 is the type given to root node in
                    // SpoonGumTreeBuilder
                    throw new IllegalStateException(
                            "non-root node " + m.first + " had no mapped Spoon object");
                }
            } else {
                mapping.put(spoonSrc, spoonDst);
            }
        }

        mapping.inferAdditionalMappings(mapping.asList());
        return mapping;
    }

    private List<Pair<CtElement, CtElement>> asList() {
        return srcToDst.entrySet().stream()
                .map(srcAndDst -> new Pair<>(srcAndDst.getKey(), srcAndDst.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Infer additional node matches. It is done by iterating over all pairs of matched nodes, and
     * for each pair, descending down into the tree incrementally and matching nodes that
     * gumtree-spoon-ast-diff is known to ignore. See <a
     * href="https://github.com/SpoonLabs/gumtree-spoon-ast-diff/blob/dae908192bee7773b38d149baff831ee616ec524/src/main/java/gumtree/spoon/builder/TreeScanner.java#L71-L84">TreeScanner</a>
     * to see how nodes are ignored in gumtree-spoon-ast-diff. The process is repeated for each pair
     * of newly matched nodes, until no new matches can be found.
     *
     * @param matches Pairs of matched nodes, as computed by GumTree/gumtree-spoon-ast-diff.
     */
    private void inferAdditionalMappings(List<Pair<CtElement, CtElement>> matches) {
        while (!matches.isEmpty()) {
            List<Pair<CtElement, CtElement>> newMatches = new ArrayList<>();
            for (Pair<CtElement, CtElement> srcAndDst : matches) {
                newMatches.addAll(
                        inferAdditionalMappings(srcAndDst.getFirst(), srcAndDst.getSecond()));
            }
            matches = newMatches;
        }
    }

    private List<Pair<CtElement, CtElement>> inferAdditionalMappings(CtElement src, CtElement dst) {
        List<CtElement> srcChildren = src.getDirectChildren();
        List<CtElement> dstChildren = dst.getDirectChildren();
        List<Pair<CtElement, CtElement>> newMatches = new ArrayList<>();

        int srcIdx = 0;
        int dstIdx = 0;

        while (srcIdx < srcChildren.size() && dstIdx < dstChildren.size()) {
            CtElement srcChild = srcChildren.get(srcIdx);
            CtElement dstChild = dstChildren.get(dstIdx);

            if (srcToDst.containsKey(srcChild) || !GumtreeSpoonAstDiff.isToIgnore(srcChild)) {
                srcIdx++;
            } else if (dstToSrc.containsKey(dstChild)
                    || !GumtreeSpoonAstDiff.isToIgnore(dstChild)) {
                dstIdx++;
            } else {
                put(srcChild, dstChild);
                newMatches.add(new Pair<>(srcChild, dstChild));
                srcIdx++;
                dstIdx++;
            }
        }

        return newMatches;
    }

    /**
     * Get the element mapped to this element in the mapping. This is a two-way method: if the
     * element passed in is a destination, its corresponding source is fetched, and vice versa.
     *
     * @param e The element to fetch a mapped element for
     * @return The mapped element
     */
    public CtElement get(CtElement e) {
        CtElement mappedDst = srcToDst.get(e);
        CtElement mappedSrc = dstToSrc.get(e);

        if (mappedDst != null) {
            return mappedDst;
        } else if (mappedSrc != null) {
            return mappedSrc;
        } else {
            throw new IllegalArgumentException("Element not mapped: " + e);
        }
    }

    private void put(CtElement src, CtElement dst) {
        srcToDst.put(src, dst);
        dstToSrc.put(dst, src);
    }

    private static CtElement getSpoonNode(Tree gumtreeNode) {
        return (CtElement) gumtreeNode.getMetadata(SpoonGumTreeBuilder.SPOON_OBJECT);
    }

    private String formatEntry(Map.Entry<CtElement, CtElement> entry) {
        return "(" + entry.getKey() + ", " + entry.getValue() + ")";
    }

    @Override
    public String toString() {
        return "SpoonMapping{"
                + "srcs="
                + srcToDst.entrySet().stream().map(this::formatEntry).collect(Collectors.toList())
                + ", dsts="
                + dstToSrc.entrySet().stream().map(this::formatEntry).collect(Collectors.toList())
                + '}';
    }
}
