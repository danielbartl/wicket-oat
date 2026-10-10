package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatVariant;
import dev.jbaby.wicket.oat.components.table.OatActionsColumn;
import dev.jbaby.wicket.oat.util.ObjectModel;
import dev.jbaby.wicket.oat.util.SerializableBiConsumer;
import dev.jbaby.wicket.oat.util.SerializableBiFunction;
import dev.jbaby.wicket.oat.util.SerializableFunction;
import dev.jbaby.wicket.oat.util.SerializableSupplier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A table of records with everything to manage them: "New" above the table, Edit and
 * Delete on every row, a dialog to edit a record in, and a confirmation before
 * deleting one.
 * <pre>{@code
 * OatCrud<Customer, String> customers = new OatCrud<>("customers", columns, provider, 20)
 *         .setEditor((id, customer) -> new CustomerFields(id, customer))   // your fields, see below
 *         .setNewItem(Customer::new)
 *         .setTitle(Customer::name)
 *         .onSave((target, customer) -> repository.save(customer))
 *         .onDelete((target, customer) -> repository.delete(customer));
 * add(customers);
 * }</pre>
 * <ul>
 * <li>The editor is any component made of form fields: a panel created with the id it
 * is given and a {@link CompoundPropertyModel} over the record. Pass the model to the
 * panel's {@code super(id, model)} and Oat fields with a property's name as their id -
 * {@code new OatTextField<>("name")} - find their value and label by themselves.</li>
 * <li>Confirm ("Save") validates the fields. Only when they are all valid are they
 * written to the record and {@link #onSave} called; Cancel leaves the record as it was.
 * Report an error there, e.g. {@code nameField.error("Already taken")}, to keep the
 * dialog open; otherwise it closes and the table is re-rendered.</li>
 * <li>A record to edit is loaded through the provider's {@code model(...)}, so with a
 * {@code LoadableDetachableModel} it is fresh from the database when it's saved.</li>
 * <li>"New" shows only with {@link #setNewItem}, Edit only with an editor, and Delete
 * only with {@link #onDelete}.</li>
 * </ul>
 * The texts ({@code OatCrud.*}) are translated; {@link #getDataTable()} gives the
 * table for its other features - selection, a column chooser, CSV export.
 *
 * @param <T> the record type
 * @param <S> the sort property type
 */
public class OatCrud<T, S> extends Panel {

    private final ISortableDataProvider<T, S> provider;
    private final OatDataTable<T, S> table;
    private final OatDialog editor;
    private final OatConfirmDialog confirm;

    private SerializableBiFunction<String, IModel<T>, ? extends Component> editorFactory;
    private SerializableSupplier<T> newItem;
    private SerializableFunction<T, String> title = String::valueOf;
    private SerializableBiConsumer<AjaxRequestTarget, T> onSave = (target, item) -> { };
    private SerializableBiConsumer<AjaxRequestTarget, T> onDelete;
    private SerializableFunction<String, ? extends Component> search;

    /** The record being edited, and whether it is a new one. */
    private IModel<T> editing;
    private boolean editingNew;

    public OatCrud(String id, List<? extends IColumn<T, S>> columns, ISortableDataProvider<T, S> provider, long rowsPerPage) {
        super(id);
        this.provider = Objects.requireNonNull(provider);
        setOutputMarkupId(true);

        OatActionsColumn<T, S> actions = new OatActionsColumn<>();
        actions.addAction(new StringResourceModel("OatCrud.edit", this).setDefaultValue("Edit"), this::edit)
                .setVisibleWhen(item -> editorFactory != null);
        actions.addAction(new StringResourceModel("OatCrud.delete", this).setDefaultValue("Delete"), this::askToDelete)
                .setVariant(OatVariant.DANGER)
                .setVisibleWhen(item -> onDelete != null);
        List<IColumn<T, S>> all = new ArrayList<>(columns);
        all.add(actions);

        table = new OatDataTable<>("table", all, provider, rowsPerPage);
        table.setToolbar(this::newToolbar);
        add(table);

        editor = new OatDialog("editor", new IModel<String>() {
            @Override
            public String getObject() {
                if (editingNew || editing == null) {
                    return new StringResourceModel("OatCrud.newTitle", OatCrud.this).setDefaultValue("New entry").getObject();
                }
                return new StringResourceModel("OatCrud.editTitle", OatCrud.this)
                        .setParameters(title.apply(editing.getObject())).setDefaultValue("Edit").getObject();
            }
        }) {
            @Override
            protected void onConfirm(AjaxRequestTarget target) {
                save(target);
            }
        };
        editor.setConfirmLabel(new StringResourceModel("OatCrud.save", this).setDefaultValue("Save"));
        editor.setBusyIndicator(true);
        add(editor);

        confirm = new OatConfirmDialog("confirm");
        confirm.setConfirmLabel(new StringResourceModel("OatCrud.delete", this).setDefaultValue("Delete"));
        add(confirm);
    }

    private Component newToolbar(String id) {
        Fragment toolbar = new Fragment(id, "toolbarFragment", this);
        toolbar.add(search != null ? search.apply("search") : new WebMarkupContainer("search").setVisible(false));
        toolbar.add(new OatButton("new", new StringResourceModel("OatCrud.new", this).setDefaultValue("New")) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                create(target);
            }

            @Override
            protected void onConfigure() {
                super.onConfigure();
                setVisible(newItem != null && editorFactory != null);
            }
        });
        return toolbar;
    }

    /**
     * The fields to edit a record with, created with the id they are given and a
     * {@link CompoundPropertyModel} over the record each time the dialog opens.
     */
    public OatCrud<T, S> setEditor(SerializableBiFunction<String, IModel<T>, ? extends Component> editor) {
        this.editorFactory = editor;
        return this;
    }

    /** Creates the record "New" edits, e.g. {@code Customer::new}; without one there's no "New". */
    public OatCrud<T, S> setNewItem(SerializableSupplier<T> newItem) {
        this.newItem = newItem;
        return this;
    }

    /** A record's name in the dialog's title and the delete question, e.g. {@code Customer::name}. */
    public OatCrud<T, S> setTitle(SerializableFunction<T, String> title) {
        this.title = Objects.requireNonNull(title);
        return this;
    }

    /** Stores a valid record, new or changed. Report an error on the dialog's fields to keep it open. */
    public OatCrud<T, S> onSave(SerializableBiConsumer<AjaxRequestTarget, T> onSave) {
        this.onSave = Objects.requireNonNull(onSave);
        return this;
    }

    /** Deletes a record once the user confirmed it; without this there's no Delete. */
    public OatCrud<T, S> onDelete(SerializableBiConsumer<AjaxRequestTarget, T> onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    /**
     * A component beside "New", e.g. a search field: have the provider read its model,
     * and in its Ajax handler call {@code crud.getDataTable().getTable().setCurrentPage(0)}
     * and {@code target.add(crud.getDataTable())}. Created with the id it is given.
     */
    public OatCrud<T, S> setSearch(SerializableFunction<String, ? extends Component> search) {
        this.search = search;
        table.setToolbar(this::newToolbar);
        return this;
    }

    /** Opens the dialog for a new record. */
    public OatCrud<T, S> create(AjaxRequestTarget target) {
        if (newItem == null || editorFactory == null) {
            throw new IllegalStateException("Creating needs setNewItem(...) and setEditor(...)");
        }
        T item = newItem.get();
        open(target, new ObjectModel<>(item), true);
        return this;
    }

    /** Opens the dialog for a record. */
    public OatCrud<T, S> edit(AjaxRequestTarget target, T item) {
        if (editorFactory == null) {
            throw new IllegalStateException("Editing needs setEditor(...)");
        }
        open(target, provider.model(item), false);
        return this;
    }

    private void open(AjaxRequestTarget target, IModel<T> model, boolean isNew) {
        editing = model;
        editingNew = isNew;
        Component fields = editorFactory.apply(OatDialog.BODY_ID, new CompoundPropertyModel<>(model));
        if (fields == null || !OatDialog.BODY_ID.equals(fields.getId())) {
            throw new IllegalArgumentException("The editor must use the id passed to the factory (\"" + OatDialog.BODY_ID
                    + "\"), but was " + (fields == null ? "null" : "\"" + fields.getId() + "\""));
        }
        editor.setBody(fields);
        editor.open(target);
    }

    private void save(AjaxRequestTarget target) {
        onSave.accept(target, editing.getObject());
        target.add(table);
    }

    private void askToDelete(AjaxRequestTarget target, T item) {
        IModel<T> model = provider.model(item);
        String name = title.apply(item);
        confirm.ask(target,
                new StringResourceModel("OatCrud.deleteQuestion", this).setParameters(name).setDefaultValue("Delete?"),
                new StringResourceModel("OatCrud.deleteDetail", this).setDefaultValue("This can't be undone."),
                t -> {
                    onDelete.accept(t, model.getObject());
                    model.detach();
                    t.add(table);
                });
    }

    /** The table, e.g. for selection and bulk actions, a column chooser or CSV export. */
    public OatDataTable<T, S> getDataTable() {
        return table;
    }

    /** The edit dialog. */
    public OatDialog getEditor() {
        return editor;
    }

    @Override
    protected void onDetach() {
        if (editing != null) {
            editing.detach();
        }
        super.onDetach();
    }
}
