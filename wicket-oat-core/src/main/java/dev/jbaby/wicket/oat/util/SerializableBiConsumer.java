package dev.jbaby.wicket.oat.util;

import java.io.Serializable;
import java.util.function.BiConsumer;

/**
 * A serializable bi-consumer for Wicket components.
 */
@FunctionalInterface
public interface SerializableBiConsumer<T, U> extends BiConsumer<T, U>, Serializable {
}
