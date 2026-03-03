package dev.jbaby.wicket.oat.util;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * A serializable consumer for Wicket components.
 */
@FunctionalInterface
public interface SerializableConsumer<T> extends Consumer<T>, Serializable {
}
