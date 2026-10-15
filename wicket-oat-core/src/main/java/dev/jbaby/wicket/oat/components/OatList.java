package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.SerializableBiFunction;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import java.util.List;
import java.util.Objects;

/**
 * Rows of things - people, documents, absences - each with a title and a line under it,
 * and room for a picture in front and a badge or a button at the end; the list a phone
 * app shows on most screens:
 * <pre>{@code
 * add(new OatList<>("team", () -> people.all(), Person::name)
 *         .setSubtitle(Person::role)
 *         .setLeading((id, person) -> new OatAvatar(id, person.getObject().initials()))
 *         .setTrailing((id, person) -> new OatBadge(id, person.getObject().status()))
 *         .setOnClick((target, person) -> setResponsePage(PersonPage.class, PersonPage.parameters(person))));
 * }</pre>
 * on any tag, e.g. {@code <div wicket:id="team"></div>}. The rows are shown in the order
 * given, read on every render. The factories for the parts in front and at the end get
 * the id to use and the row's model.
 * <p>
 * With {@link #setOnClick} the title becomes a link that covers the whole row, while
 * buttons at the end stay clickable on their own. While there are no rows,
 * {@link #setEmptyState} shows a component instead of the list.
 *
 * @param <T> the row type
 */
public class OatList<T> extends Panel {

    private static final String LEADING_ID = "leading";
    private static final String TRAILING_ID = "trailing";
    private static final String EMPTY_ID = "empty";

    private final IModel<? extends List<T>> rows;
    private SerializableFunction<T, ?> subtitle;
    private SerializableBiFunction<String, IModel<T>, ? extends Component> leading;
    private SerializableBiFunction<String, IModel<T>, ? extends Component> trailing;
    private SerializableBiConsumer<AjaxRequestTarget, T> onClick;

    /**
     * @param rows the rows, in the order to show them
     * @param title a row's title, e.g. a person's name
     */
    public OatList(String id, IModel<? extends List<T>> rows, SerializableFunction<T, ?> title) {
        super(id);
        this.rows = Objects.requireNonNull(rows);
        Objects.requireNonNull(title);

        WebMarkupContainer list = new WebMarkupContainer("list") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!rows.getObject().isEmpty());
            }
        };
        add(list);
        list.add(new ListView<T>("rows", rows) {
            @Override
            protected void populateItem(ListItem<T> item) {
                T row = item.getModelObject();
                item.add(part(LEADING_ID, leading, item.getModel()));

                AjaxLink<Void> link = new AjaxLink<>("link") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        onClick.accept(target, row);
                    }
                };
                // Without an action, just the title
                link.setEnabled(onClick != null);
                link.setRenderBodyOnly(onClick == null);
                link.add(new Label("title", Model.of(String.valueOf(title.apply(row)))));
                item.add(link);

                Object text = subtitle != null ? subtitle.apply(row) : null;
                item.add(new Label("subtitle", Model.of(text != null ? text.toString() : null))
                        .setVisible(text != null && !text.toString().isBlank()));

                item.add(part(TRAILING_ID, trailing, item.getModel()));
            }
        });
        add(new WebMarkupContainer(EMPTY_ID).setVisible(false));
    }

    /** The part of a row in front or at the end, made by its factory, or nothing. */
    private Component part(String id, SerializableBiFunction<String, IModel<T>, ? extends Component> factory, IModel<T> row) {
        if (factory == null) {
            return new WebMarkupContainer(id).setVisible(false);
        }
        Component part = factory.apply(id, row);
        if (part == null || !id.equals(part.getId())) {
            throw new IllegalArgumentException("The component must use the id passed to the factory (\"" + id + "\"), but was "
                    + (part == null ? "null" : "\"" + part.getId() + "\""));
        }
        return part;
    }

    /** A line under a row's title, e.g. a role or a date. */
    public OatList<T> setSubtitle(SerializableFunction<T, ?> subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    /** Something in front of each row, e.g. an {@code OatAvatar} or an {@code OatIcon}. */
    public OatList<T> setLeading(SerializableBiFunction<String, IModel<T>, ? extends Component> leading) {
        this.leading = leading;
        return this;
    }

    /** Something at the end of each row, e.g. an {@code OatBadge} or a button. */
    public OatList<T> setTrailing(SerializableBiFunction<String, IModel<T>, ? extends Component> trailing) {
        this.trailing = trailing;
        return this;
    }

    /**
     * Makes each row a link that runs this, e.g. to open the row's page with
     * {@code setResponsePage(...)} or a dialog to edit it.
     */
    public OatList<T> setOnClick(SerializableBiConsumer<AjaxRequestTarget, T> onClick) {
        this.onClick = onClick;
        return this;
    }

    /**
     * Shows a component instead of the list while there are no rows, e.g.
     * {@code list.setEmptyState(id -> new OatEmptyState(id, "Everyone's in"))}. The factory
     * gets the id to use.
     */
    public OatList<T> setEmptyState(SerializableFunction<String, ? extends Component> emptyState) {
        Component empty = emptyState.apply(EMPTY_ID);
        if (empty == null || !EMPTY_ID.equals(empty.getId())) {
            throw new IllegalArgumentException("The empty state must use the id passed to the factory (\"" + EMPTY_ID
                    + "\"), but was " + (empty == null ? "null" : "\"" + empty.getId() + "\""));
        }
        empty.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                component.setVisible(rows.getObject().isEmpty());
            }
        });
        replace(empty);
        return this;
    }

    @Override
    protected void onDetach() {
        rows.detach();
        super.onDetach();
    }
}
