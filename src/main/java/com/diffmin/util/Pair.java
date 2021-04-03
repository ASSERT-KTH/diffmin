package com.diffmin.util;

/**
 * Class to create a Pair data structure.
 *
 * @param <T> Generic type for first element inside Pair
 * @param <P> Generic type for second element inside Pair
 */
public class Pair<T, P> {
    private final T first;
    private final P second;

    /**
     * Constructs a Pair instance.
     *
     * @param first first element
     * @param second second element
     */
    public Pair(T first, P second) {
        this.first = first;
        this.second = second;
    }

    /** Returns the first element. */
    public T getFirst() {
        return this.first;
    }

    /** Returns the second element. */
    public P getSecond() {
        return this.second;
    }
}
