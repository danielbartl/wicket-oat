package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.behaviors.ButtonGroupBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;

/**
 * A container component that renders as an Oat Button Group.
 *
 * <pre>
 * &lt;div wicket:id="group"&gt;
 *     &lt;button wicket:id="button1"&gt;Left&lt;/button&gt;
 *     &lt;button wicket:id="button2"&gt;Center&lt;/button&gt;
 *     &lt;button wicket:id="button3"&gt;Right&lt;/button&gt;
 * &lt;/div&gt;
 * </pre>
 */
public class ButtonGroup extends WebMarkupContainer {

    public ButtonGroup(String id) {
        super(id);
        add(new ButtonGroupBehavior());
    }
}
