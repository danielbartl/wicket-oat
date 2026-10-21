package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.SerializableBiFunction;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

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
 * given, read once per request. The factories for the parts in front and at the end get
 * the id to use and the row's model; a row's model finds it by its position in the
 * list, as a {@link ListView}'s does. The parts are on a {@code <div>}, so a button
 * there goes in a {@code Panel} or {@code Fragment} of your own, on a {@code <button>}
 * tag. The list has a markup id, so {@code target.add(list)} shows changed rows.
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
    // The rows, read once per request: each row's model and the empty state read them through this
    private final IModel<List<T>> loaded;
    private final ListView<T> rowsView;
    private SerializableFunction<T, ?> subtitle;
    private SerializableBiFunction<String, IModel<T>, ? extends Component> leading;
    private SerializableBiFunction<String, IModel<T>, ? extends Component> trailing;
    private SerializableBiConsumer<AjaxRequestTarget, T> onClick;

    /**
     * @param rows the rows, in the order to show them; {@code null} counts as none
     * @param title a row's title, e.g. a person's name
     */
    public OatList(String id, IModel<? extends List<T>> rows, SerializableFunction<T, ?> title) {
        super(id);
        this.rows = Objects.requireNonNull(rows);
        Objects.requireNonNull(title);
        this.loaded = LoadableDetachableModel.of(() -> {
            List<T> list = rows.getObject();
            return list != null ? list : List.of();
        });
        setOutputMarkupId(true);

        WebMarkupContainer list = new WebMarkupContainer("list") {
            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(!loaded.getObject().isEmpty());
            }
        };
        add(list);
        rowsView = new ListView<T>("rows", loaded) {
            @Override
            protected void populateItem(ListItem<T> item) {
                IModel<T> row = item.getModel();
                item.add(part(LEADING_ID, leading, row));

                // The row from its model on click, so the link doesn't keep the row with the page
                MarkupContainer link = onClick == null
                        ? (MarkupContainer) new WebMarkupContainer("link").setRenderBodyOnly(true)
                        : new AjaxLink<T>("link", row) {
                            @Override
                            public void onClick(AjaxRequestTarget target) {
                                onClick.accept(target, getModelObject());
                            }
                        };
                link.add(new Label("title", row.map(r -> title.apply(r))));
                item.add(link);

                IModel<String> text = row.map(r -> subtitle != null ? subtitle.apply(r) : null).map(Object::toString);
                item.add(new Label("subtitle", text) {
                    @Override
                    protected void onConfigure() {
                        super.onConfigure();
                        String value = text.getObject();
                        setVisible(value != null && !value.isBlank());
                    }
                });

                item.add(part(TRAILING_ID, trailing, row));
            }
        };
        list.add(rowsView);
        add(new WebMarkupContainer(EMPTY_ID).setVisible(false));
    }

    /** The part of a row in front or at the end, made by its factory, or nothing. */
    private Component part(String id, SerializableBiFunction<String, IModel<T>, ? extends Component> factory, IModel<T> row) {
        if (factory == null) {
            return new WebMarkupContainer(id).setVisible(false);
        }
        return checkId(id, factory.apply(id, row), "The component");
    }

    private static Component checkId(String id, Component component, String what) {
        if (component == null || !id.equals(component.getId())) {
            throw new IllegalArgumentException(what + " must use the id passed to the factory (\"" + id + "\"), but was "
                    + (component == null ? "null" : "\"" + component.getId() + "\""));
        }
        return component;
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

    /**
     * Something at the end of each row, e.g. an {@code OatBadge}, or a panel with a button:
     * the part is on a {@code <div>}, which can't be a button itself.
     */
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
        Component empty = checkId(EMPTY_ID, emptyState.apply(EMPTY_ID), "The empty state");
        empty.add(new Behavior() {
            @Override
            public void onConfigure(Component component) {
                component.setVisible(loaded.getObject().isEmpty());
            }
        });
        replace(empty);
        return this;
    }

    /**
     * Keeps the rows' components from one render to the next instead of making them anew,
     * as {@link ListView#setReuseItems} does: needed for form fields in the rows, e.g. a
     * switch at the end of each, so that what the user entered survives a failed
     * validation. Off by default. Rows are matched by their position, so the parts in
     * front and at the end must read the row through its model on every render, e.g.
     * {@code OatAvatar.initials(id, person.map(Person::initials))}, not
     * {@code person.getObject().initials()} once.
     */
    public OatList<T> setReuseItems(boolean reuseItems) {
        rowsView.setReuseItems(reuseItems);
        return this;
    }

    @Override
    protected void onDetach() {
        loaded.detach();
        rows.detach();
        super.onDetach();
    }
}
