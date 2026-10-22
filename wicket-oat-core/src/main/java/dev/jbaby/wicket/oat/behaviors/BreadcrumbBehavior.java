package dev.jbaby.wicket.oat.behaviors;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;

/**
 * Behavior that applies Oat's Breadcrumb styling to a &lt;nav&gt; element.
 * It adds aria-label="Breadcrumb".
 *
 * @deprecated unused since {@code OatBreadcrumb} names its {@code <nav>} itself, in the
 *             user's language; write the {@code aria-label} of a {@code <nav>} of your
 *             own in its markup
 */
@Deprecated(since = "0.5.0", forRemoval = true)
public class BreadcrumbBehavior extends Behavior {

    @Override
    public void onComponentTag(Component component, ComponentTag tag) {
        super.onComponentTag(component, tag);
        tag.put("aria-label", "Breadcrumb");
    }
}
