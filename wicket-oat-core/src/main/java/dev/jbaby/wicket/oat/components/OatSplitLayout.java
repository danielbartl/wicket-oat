package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import java.util.Objects;

/**
 * Two panes side by side - a list and a document, an editor and its preview - whose
 * first pane the user can make wider or narrower by dragging its corner handle:
 * <pre>{@code
 * add(new OatSplitLayout("split", id -> new FolderList(id), id -> new MessageView(id)).setSplit(30));
 * }</pre>
 * It needs no JavaScript: the first pane is resizable with CSS ({@code resize}), so the
 * handle is the browser's own, in its bottom corner, and it can't be moved with the
 * keyboard. {@link Orientation#VERTICAL} stacks the panes, the first resizable in
 * height. On narrow screens the panes are stacked and not resizable. Each pane scrolls
 * on its own when its content is larger.
 */
public class OatSplitLayout extends Panel {

    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    private Orientation orientation = Orientation.HORIZONTAL;
    private int split = 50;

    /** @param first creates the first pane, @param second the second, each with the id it is given */
    public OatSplitLayout(String id, SerializableFunction<String, ? extends Component> first,
                         SerializableFunction<String, ? extends Component> second) {
        super(id);
        WebMarkupContainer firstPane = new WebMarkupContainer("firstPane");
        // On the pane itself, not "container > pane": in development mode Wicket keeps
        // <wicket:panel> between them. Classes rather than a style, which the CSP would block.
        firstPane.add(AttributeModifier.append("class", (IModel<String>) () -> orientation == Orientation.VERTICAL
                ? "oat-split-first-vertical oat-pane-v-" + split : "oat-pane-" + split));
        firstPane.add(pane("first", first));
        add(firstPane);
        add(pane("second", second));
    }

    private static Component pane(String id, SerializableFunction<String, ? extends Component> factory) {
        Component component = Objects.requireNonNull(factory.apply(id));
        if (!id.equals(component.getId())) {
            throw new IllegalArgumentException("A pane must use the id passed to its factory (\"" + id + "\"), but was \""
                    + component.getId() + "\"");
        }
        return component;
    }

    /** Side by side ({@code HORIZONTAL}, the default) or one above the other ({@code VERTICAL}). */
    public OatSplitLayout setOrientation(Orientation orientation) {
        this.orientation = Objects.requireNonNull(orientation);
        return this;
    }

    /**
     * The first pane's starting share in percent: 20, 30, 40, 50 (the default), 60, 70 or
     * 80. The user can change it by dragging.
     */
    public OatSplitLayout setSplit(int percent) {
        if (percent < 20 || percent > 80 || percent % 10 != 0) {
            throw new IllegalArgumentException("The split must be 20, 30, ..., 80 percent: " + percent);
        }
        this.split = percent;
        return this;
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.append("class", orientation == Orientation.VERTICAL ? "oat-split oat-split-vertical" : "oat-split", " ");
    }
}
