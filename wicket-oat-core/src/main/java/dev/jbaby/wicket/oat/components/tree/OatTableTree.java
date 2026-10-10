package dev.jbaby.wicket.oat.components.tree;

import dev.jbaby.wicket.oat.components.OatPagingNavigator;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.tree.ISortableTreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.TableTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.string.Strings;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * A tree grid: a table whose rows are nodes of a tree - accounts and sub-accounts with
 * their balances, a project's tasks and subtasks - built on Wicket's {@link TableTree}:
 * <pre>{@code
 * List<IColumn<Account, String>> columns = List.of(
 *         new TreeColumn<>(Model.of("Account")),                               // the tree, indented
 *         new OatNumberColumn<>(Model.of("Balance"), Account::balance).setCurrency(EUR));
 * add(new OatTableTree<>("accounts", columns, new AccountProvider(), 50).setLabel(Account::name));
 * }</pre>
 * The {@link TreeColumn} shows each node indented under its parent, with an expand
 * button (see {@link OatTreeNode}) and its {@linkplain #setLabel label}; the other
 * columns are ordinary data table columns, including Oat's. Children are loaded when
 * their parent is expanded. Rows are paged ({@link OatPagingNavigator}), sortable
 * headers set {@code aria-sort} when the provider is an {@link ISortableTreeProvider},
 * and an empty tree shows {@code OatTableTree.empty}. After a node is expanded or
 * collapsed, focus returns to its button.
 *
 * @param <T> the node type
 * @param <S> the sort property type
 */
public class OatTableTree<T, S> extends TableTree<T, S> {

    private SerializableFunction<T, String> label = String::valueOf;
    private SerializableFunction<T, String> icon;
    private SerializableBiConsumer<AjaxRequestTarget, T> onSelect;
    private T selected;

    public OatTableTree(String id, List<? extends IColumn<T, S>> columns, ITreeProvider<T> provider, long rowsPerPage) {
        this(id, columns, provider, rowsPerPage, null);
    }

    /** @param state the expanded nodes, or {@code null} for a new set */
    @SuppressWarnings("unchecked")
    public OatTableTree(String id, List<? extends IColumn<T, S>> columns, ITreeProvider<T> provider, long rowsPerPage,
                        IModel<? extends Set<T>> state) {
        super(id, columns, provider, rowsPerPage, state);
        DataTable<T, S> table = getTable();
        table.add(AttributeModifier.append("class", "w-100 align-left small"));
        if (provider instanceof ISortStateLocator<?>) {
            ISortStateLocator<S> locator = (ISortStateLocator<S>) provider;
            table.addTopToolbar(new HeadersToolbar<>(table, locator) {
                @Override
                protected WebMarkupContainer newSortableHeader(String headerId, S property, ISortStateLocator<S> sortLocator) {
                    WebMarkupContainer header = super.newSortableHeader(headerId, property, sortLocator);
                    header.add(AttributeModifier.replace("aria-sort", (IModel<String>) () -> {
                        SortOrder order = sortLocator.getSortState().getPropertySortOrder(property);
                        return order == SortOrder.ASCENDING ? "ascending" : order == SortOrder.DESCENDING ? "descending" : null;
                    }));
                    return header;
                }
            });
        } else {
            table.addTopToolbar(new HeadersToolbar<>(table, null));
        }
        table.addBottomToolbar(new NoRecordsToolbar(table, new StringResourceModel("OatTableTree.empty", this).setDefaultValue("Nothing to show")));
        table.addBottomToolbar(new NavigationToolbar(table) {
            @Override
            protected PagingNavigator newPagingNavigator(String navigatorId, DataTable<?, ?> dataTable) {
                return new OatPagingNavigator(navigatorId, dataTable);
            }
        });
    }

    /** A node's label in the {@link TreeColumn} ({@code toString()} by default). */
    public OatTableTree<T, S> setLabel(SerializableFunction<T, String> label) {
        this.label = Objects.requireNonNull(label);
        return this;
    }

    /** An {@link dev.jbaby.wicket.oat.components.OatIcon} name for a node, or {@code null} for none. */
    public OatTableTree<T, S> setIcon(SerializableFunction<T, String> icon) {
        this.icon = icon;
        return this;
    }

    /** Makes labels links that select their row and then run this; the selected label has {@code aria-current}. */
    public OatTableTree<T, S> onSelect(SerializableBiConsumer<AjaxRequestTarget, T> onSelect) {
        this.onSelect = onSelect;
        return this;
    }

    @Override
    public Component newNodeComponent(String id, IModel<T> model) {
        return new OatTreeNode<>(id, this, model) {
            @Override
            protected Component createContent(String contentId, IModel<T> contentModel) {
                return newContentComponent(contentId, contentModel);
            }

            @Override
            protected String getName() {
                return label.apply(getModelObject());
            }

            @Override
            protected void onToggled(AjaxRequestTarget target) {
                // The whole table is re-rendered with new ids; focus this row's button again
                Item<?> row = findParent(Item.class);
                while (row != null && !(row.getParent() != null && row.getParent().getParent() == getTable().getBody())) {
                    row = row.findParent(Item.class);
                }
                if (row != null) {
                    target.appendJavaScript("(function(t){var b=t&&t.querySelectorAll('tbody > tr')[" + row.getIndex()
                            + "];b=b&&b.querySelector('.oat-junction');if(b)b.focus();})(document.getElementById('"
                            + Strings.escapeMarkup(OatTableTree.this.getMarkupId()) + "'))");
                }
            }
        };
    }

    @Override
    protected Component newContentComponent(String id, IModel<T> model) {
        return OatTree.content(id, model, label, icon, onSelect == null ? null : (target, node) -> {
            selected = node;
            target.add(this);
            onSelect.accept(target, node);
        }, () -> selected != null && selected.equals(model.getObject()));
    }

    @Override
    protected void onComponentTag(org.apache.wicket.markup.ComponentTag tag) {
        super.onComponentTag(tag);
        tag.append("class", "oat-table-tree", " ");
    }
}
