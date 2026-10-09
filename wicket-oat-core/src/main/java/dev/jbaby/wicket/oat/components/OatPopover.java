package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * A button that opens a small panel next to it - filters for a table, a quick-edit
 * form, details about an item - using Oat's dropdown ({@code ot-dropdown}) and the
 * browser's popover. Unlike {@link OatDropdown}, its content is any component:
 * <pre>{@code
 * add(new OatPopover("filters", "Filters", id -> new InvoiceFiltersPanel(id, filter)));
 * }</pre>
 * on {@code <div wicket:id="filters"></div>}. It closes when the user clicks outside it
 * or presses Escape; {@link #close} closes it from an Ajax handler, e.g. after applying
 * the filters. The content is rendered with the page; re-render it with
 * {@code target.add(popover.getContent())} to update it.
 */
public class OatPopover extends Panel {

    /** The component id the content must use. */
    public static final String CONTENT_ID = "content";

    private final WebMarkupContainer popover;
    private final WebMarkupContainer trigger;

    public OatPopover(String id, String triggerLabel, SerializableFunction<String, ? extends Component> content) {
        this(id, Model.of(triggerLabel), content);
    }

    /** @param content creates the content, with the id it is given */
    public OatPopover(String id, IModel<String> triggerLabel, SerializableFunction<String, ? extends Component> content) {
        super(id);
        setRenderBodyOnly(true);

        popover = new WebMarkupContainer("popover");
        popover.setOutputMarkupId(true);
        add(popover);

        trigger = new WebMarkupContainer("trigger");
        trigger.add(new Label("triggerLabel", triggerLabel));
        trigger.add(AttributeModifier.replace("popovertarget", (IModel<String>) popover::getMarkupId));
        add(trigger);

        Component body = content.apply(CONTENT_ID);
        if (body == null || !CONTENT_ID.equals(body.getId())) {
            throw new IllegalArgumentException("The popover content must use the id passed to the factory (\""
                    + CONTENT_ID + "\"), but was " + (body == null ? "null" : "\"" + body.getId() + "\""));
        }
        body.setOutputMarkupId(true);
        popover.add(body);
    }

    /** The button that opens the popover, e.g. to change its style or add an {@code aria-label}. */
    public WebMarkupContainer getTrigger() {
        return trigger;
    }

    /** The content, e.g. to re-render it with {@code target.add(...)}. */
    public Component getContent() {
        return popover.get(CONTENT_ID);
    }

    /** Closes the popover, e.g. after its form was submitted. */
    public OatPopover close(IPartialPageRequestHandler target) {
        target.appendJavaScript("(function(p){if(p&&p.matches(':popover-open'))p.hidePopover();})(document.getElementById('"
                + popover.getMarkupId() + "'))");
        return this;
    }
}
