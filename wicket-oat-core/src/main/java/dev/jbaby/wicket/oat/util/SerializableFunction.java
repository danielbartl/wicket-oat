package dev.jbaby.wicket.oat.util;

import java.io.Serializable;
import java.util.function.Function;

/**
 * A serializable function for Wicket components.
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}
