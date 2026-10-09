package dev.jbaby.wicket.oat.behaviors;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.WicketOats;
import dev.jbaby.wicket.oat.util.SerializableConsumer;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Opens a menu of actions where the user right-clicks a component - a table row, a
 * card, a file - or presses Shift+F10 or the Menu key while it has focus:
 * <pre>{@code
 * row.add(Oat.Behaviors.contextMenu()
 *         .addAction("Open", target -> open(invoice))
 *         .addAction("Duplicate", target -> duplicate(invoice))
 *         .addAction(Model.of("Delete"), OatVariant.DANGER, target -> confirm.ask(...)));
 * }</pre>
 * The menu looks like Oat's dropdown menu, opens at the pointer, is used with the
 * arrow keys, and closes on Escape, Tab or a click outside it. Choosing an entry runs
 * its action over Ajax. To open it from the keyboard the component needs to be
 * focusable, e.g. a link or an element with {@code tabindex="0"}. On touch screens
 * it opens on a long press where the browser supports that (not iOS Safari), so
 * offer the actions another way too, e.g. in an {@code OatActionsColumn}.
 * <p>
 * It uses Wicket Oat's small script ({@link WicketOats#WICKET_OAT_JS}), which it adds to
 * the page; the entries' labels are passed as data, never as script.
 */
public class ContextMenuBehavior extends AbstractDefaultAjaxBehavior {

    private final List<Action> actions = new ArrayList<>();

    /** Adds an entry to the menu. */
    public ContextMenuBehavior addAction(String label, SerializableConsumer<AjaxRequestTarget> onClick) {
        return addAction(Model.of(label), OatVariant.DEFAULT, onClick);
    }

    /** Adds an entry to the menu, e.g. with {@code DANGER} for a destructive one, shown in red. */
    public ContextMenuBehavior addAction(IModel<String> label, OatVariant variant, SerializableConsumer<AjaxRequestTarget> onClick) {
        actions.add(new Action(label, variant, onClick));
        return this;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        StringBuilder entries = new StringBuilder("[");
        for (Action action : actions) {
            if (entries.length() > 1) {
                entries.append(',');
            }
            entries.append("{\"label\":").append(json(action.label().getObject()));
            if (action.variant() != null && action.variant().getValue() != null) {
                entries.append(",\"variant\":").append(json(action.variant().getValue()));
            }
            entries.append('}');
        }
        tag.put("data-oat-context-menu", entries.append(']'));
        tag.put("data-oat-context-url", getCallbackUrl());
    }

    @Override
    public void renderHead(Component component, IHeaderResponse response) {
        super.renderHead(component, response);
        response.render(JavaScriptHeaderItem.forReference(WicketOats.WICKET_OAT_JS));
    }

    @Override
    protected void respond(AjaxRequestTarget target) {
        int index = getComponent().getRequest().getRequestParameters().getParameterValue("action").toInt(-1);
        if (index >= 0 && index < actions.size()) {
            actions.get(index).onClick().accept(target);
        }
    }

    @Override
    public void detach(Component component) {
        super.detach(component);
        actions.forEach(action -> action.label().detach());
    }

    /** A JSON string; the attribute itself is escaped when the tag is written. */
    private static String json(String text) {
        StringBuilder out = new StringBuilder("\"");
        for (char c : String.valueOf(text).toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.append('"').toString();
    }

    private record Action(IModel<String> label, OatVariant variant, SerializableConsumer<AjaxRequestTarget> onClick)
            implements Serializable {
    }
}
