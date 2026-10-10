package dev.jbaby.wicket.oat.components.tree;

import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.ObjectModel;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.NestedTree;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.model.IModel;


import java.util.Objects;
import java.util.Set;

/**
 * A tree of nested items - folders, categories, an org chart - that loads a node's
 * children only when it is expanded, built on Wicket's {@link NestedTree}:
 * <pre>{@code
 * OatTree<Category> categories = new OatTree<>("categories", new CategoryProvider())
 *         .setLabel(Category::name)
 *         .setIcon(category -> category.isLeaf() ? "tag" : "folder")
 *         .onSelect((target, category) -> { products.show(category); target.add(products); });
 * }</pre>
 * The {@link ITreeProvider} gives the roots, each node's children and whether it has
 * any; expanded nodes are kept in the tree's model (a set compared by the provider's
 * models, see {@code ProviderSubset}). Each node has an expand button for keyboard and
 * screen reader users (see {@link OatTreeNode}) and its label; with {@link #onSelect}
 * the label is a link and the selected node is marked with {@code aria-current}.
 * Styled with Oat's tokens ({@code .oat-tree}); Wicket's tree themes aren't needed.
 *
 * @param <T> the node type
 */
public class OatTree<T> extends NestedTree<T> {

    private SerializableFunction<T, String> label = String::valueOf;
    private SerializableFunction<T, String> icon;
    private SerializableBiConsumer<AjaxRequestTarget, T> onSelect;
    private final IModel<T> selected;

    public OatTree(String id, ITreeProvider<T> provider) {
        this(id, provider, null, new ObjectModel<>());
    }

    /**
     * @param state the expanded nodes, or {@code null} for a new set
     * @param selected the selected node, read and set by the tree
     */
    public OatTree(String id, ITreeProvider<T> provider, IModel<? extends Set<T>> state, IModel<T> selected) {
        super(id, provider, state);
        this.selected = Objects.requireNonNull(selected);
        setOutputMarkupId(true);
    }

    /** A node's label ({@code toString()} by default). */
    public OatTree<T> setLabel(SerializableFunction<T, String> label) {
        this.label = Objects.requireNonNull(label);
        return this;
    }

    /** An {@link dev.jbaby.wicket.oat.components.OatIcon} name for a node, e.g. {@code "folder"}, or {@code null} for none. */
    public OatTree<T> setIcon(SerializableFunction<T, String> icon) {
        this.icon = icon;
        return this;
    }

    /** Makes labels links that select their node and then run this. */
    public OatTree<T> onSelect(SerializableBiConsumer<AjaxRequestTarget, T> onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    public IModel<T> getSelected() {
        return selected;
    }

    /** Selects a node, re-rendering the old and the new one. */
    public OatTree<T> select(AjaxRequestTarget target, T node) {
        T old = selected.getObject();
        selected.setObject(node);
        if (old != null) {
            updateNode(old, target);
        }
        if (node != null) {
            updateNode(node, target);
        }
        return this;
    }

    @Override
    public Component newNodeComponent(String id, IModel<T> model) {
        OatTreeNode<T> node = new OatTreeNode<>(id, this, model) {
            @Override
            protected Component createContent(String contentId, IModel<T> contentModel) {
                return OatTree.this.newContentComponent(contentId, contentModel);
            }

            @Override
            protected String getName() {
                return label.apply(getModelObject());
            }
        };
        node.setOutputMarkupId(true);
        return node;
    }

    @Override
    protected Component newContentComponent(String id, IModel<T> model) {
        return content(id, model, label, icon, onSelect == null ? null : (target, node) -> {
            select(target, node);
            onSelect.accept(target, node);
        }, () -> {
            T current = selected.getObject();
            return current != null && current.equals(model.getObject());
        });
    }

    @Override
    protected void onComponentTag(ComponentTag tag) {
        super.onComponentTag(tag);
        tag.append("class", "oat-tree", " ");
    }

    @Override
    protected void onDetach() {
        selected.detach();
        super.onDetach();
    }

    /** A node's content: icon and label, the label a link when nodes can be selected. */
    static <T> Component content(String id, IModel<T> model, SerializableFunction<T, String> label,
                                 SerializableFunction<T, String> icon, SerializableBiConsumer<AjaxRequestTarget, T> onClick,
                                 IModel<Boolean> current) {
        return new OatTreeNodeContent<>(id, model, label, icon, onClick, current);
    }
}
