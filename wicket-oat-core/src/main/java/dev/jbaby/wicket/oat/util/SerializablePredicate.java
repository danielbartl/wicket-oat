package dev.jbaby.wicket.oat.util;

import java.io.Serializable;
import java.util.function.Predicate;

/**
 * A serializable predicate for Wicket components.
 */
@FunctionalInterface
public interface SerializablePredicate<T> extends Predicate<T>, Serializable {
}
