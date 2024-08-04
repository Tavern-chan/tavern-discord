package com.tavern.utilities;

import javax.annotation.Nullable;

/**
 * Contains a reference to an object
 * @param <T> The type this instance references
 */
public class Ref<T> {
    private T actual;

    public Ref() {
        this(null);
    }

    public Ref(T actual) {
        this.actual = actual;
    }

    @Nullable
    public T get() {
        return actual;
    }

    public void set(T actual) {
        this.actual = actual;
    }
}
