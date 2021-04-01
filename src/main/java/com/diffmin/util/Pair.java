package com.diffmin.util;

/**
 * Class to create a Pair data structure.
 *
 * @param <T> Generic type for first element inside Pair
 * @param <P> Generic type for second element inside Pair
 */
public class Pair<T, P> {
    public final T a;
    public final P b;

    /**
     * Constructs a Pair instance.
     *
     * @param a first element
     * @param b second element
     */
    public Pair(T a, P b) {
        this.a = a;
        this.b = b;
    }
}
