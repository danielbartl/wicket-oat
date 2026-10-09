package dev.jbaby.wicket.oat.util;

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * A serializable bi-function for Wicket components.
 */
@FunctionalInterface
public interface SerializableBiFunction<T, U, R> extends BiFunction<T, U, R>, Serializable {
}
