package dev.jbaby.wicket.oat.util;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * A serializable supplier for Wicket components.
 */
@FunctionalInterface
public interface SerializableSupplier<T> extends Supplier<T>, Serializable {
}
