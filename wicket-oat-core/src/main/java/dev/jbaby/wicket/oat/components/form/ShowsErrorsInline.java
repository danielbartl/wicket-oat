package dev.jbaby.wicket.oat.components.form;

import org.apache.wicket.Component;

/**
 * A container that shows the first error of some of its form components itself, next
 * to them - like an Oat form field, or an {@code OatEditableColumn} cell while it is
 * being edited. {@link NotShownInlineFilter} leaves those errors out, so a feedback
 * panel or toast doesn't repeat them.
 */
public interface ShowsErrorsInline {

    /** Whether this container shows the first error of {@code reporter} inline. */
    boolean showsErrorsOf(Component reporter);
}
