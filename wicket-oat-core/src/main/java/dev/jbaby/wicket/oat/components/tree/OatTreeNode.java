package dev.jbaby.wicket.oat.components.tree;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.Node;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * A node of an {@link OatTree} or {@link OatTableTree}: its expand button and its
 * content. The button is a {@code <button>} (so Enter and Space work) and a disclosure button - {@code aria-expanded}, named
 * "Expand {name}" / "Collapse {name}" ({@code OatTree.expand}/{@code OatTree.collapse})
 * - and hidden from screen readers on a leaf, which has nothing to expand.
 *
 * @param <T> the node type
 */
public abstract class OatTreeNode<T> extends Node<T> {

    private final AbstractTree<T> tree;

    public OatTreeNode(String id, AbstractTree<T> tree, IModel<T> model) {
        super(id, tree, model);
        this.tree = tree;
    }

    /** The node's name, for the button's label. */
    protected abstract String getName();

    /** Runs after the node was expanded or collapsed and the tree is being updated. */
    protected void onToggled(AjaxRequestTarget target) {
    }

    @Override
    protected MarkupContainer createJunctionComponent(String id) {
        AjaxLink<Void> junction = new AjaxLink<>(id) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                toggle();
                onToggled(target);
            }

            @Override
            public boolean isEnabled() {
                return hasChildren();
            }

            @Override
            protected void onComponentTag(ComponentTag tag) {
                super.onComponentTag(tag);
                if (hasChildren()) {
                    tag.put("aria-expanded", String.valueOf(isExpanded()));
                } else {
                    tag.put("aria-hidden", "true");
                }
            }
        };
        junction.setOutputMarkupId(true);
        junction.add(AttributeModifier.replace("aria-label", () -> hasChildren()
                ? new StringResourceModel(isExpanded() ? "OatTree.collapse" : "OatTree.expand", this)
                        .setParameters(getName()).setDefaultValue(isExpanded() ? "Collapse" : "Expand").getObject()
                : null));
        return junction;
    }

    private boolean hasChildren() {
        return tree.getProvider().hasChildren(getModelObject());
    }

    private boolean isExpanded() {
        return tree.getState(getModelObject()) == AbstractTree.State.EXPANDED;
    }

    @Override
    protected String getExpandedStyleClass(T t) {
        return "oat-junction oat-junction-expanded";
    }

    @Override
    protected String getCollapsedStyleClass() {
        return "oat-junction oat-junction-collapsed";
    }

    @Override
    protected String getOtherStyleClass() {
        return "oat-junction oat-junction-leaf";
    }

    /** The content component, as created by the tree. */
    public Component getContent() {
        return get(CONTENT_ID);
    }
}
