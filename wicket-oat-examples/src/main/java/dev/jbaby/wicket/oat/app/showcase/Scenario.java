package dev.jbaby.wicket.oat.app.showcase;

import dev.jbaby.wicket.oat.util.SerializableConsumer;
import org.apache.wicket.MarkupContainer;

import java.io.Serializable;

/**
 * One example on the showcase page, as a card of the website's component showcase shows
 * it: markup with {@code wicket:id}s, and the components that fill it. {@code title}
 * names the components and {@code code} sums up their API in a line.
 */
public record Scenario(String id, String title, String code, String markup,
                       SerializableConsumer<MarkupContainer> populate, boolean block, boolean wide)
        implements Serializable {

    public static Scenario of(String id, String title, String code, String markup,
                              SerializableConsumer<MarkupContainer> populate) {
        return new Scenario(id, title, code, markup, populate, false, false);
    }

    /** Static markup, with no components. */
    public static Scenario of(String id, String title, String code, String markup) {
        return of(id, title, code, markup, container -> {
        });
    }

    /** The preview lays its markup out as a block, instead of centering its parts in a row. */
    public Scenario asBlock() {
        return new Scenario(id, title, code, markup, populate, true, wide);
    }

    /** The card spans two columns. */
    public Scenario asWide() {
        return new Scenario(id, title, code, markup, populate, block, true);
    }
}
