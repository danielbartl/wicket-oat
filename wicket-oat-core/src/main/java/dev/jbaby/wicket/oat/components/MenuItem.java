package dev.jbaby.wicket.oat.components;

import org.apache.wicket.Page;

import java.io.Serializable;

/**
 * A single sidebar navigation entry for {@link OatAppLayout}: a label paired
 * with the Wicket {@link Page} it links to.
 */
public record MenuItem(
        String label,
        Class<? extends Page> pageClass
) implements Serializable {

    public static MenuItem of(String label, Class<? extends Page> pageClass) {
        return new MenuItem(label, pageClass);
    }
}