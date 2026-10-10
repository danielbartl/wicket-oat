package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.WicketOats;
import dev.jbaby.wicket.oat.components.form.OatMultiSelectField;
import dev.jbaby.wicket.oat.components.form.OatTextField;
import dev.jbaby.wicket.oat.components.tree.OatTableTree;
import dev.jbaby.wicket.oat.components.tree.OatTree;
import dev.jbaby.wicket.oat.components.tree.OatTreeNode;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class TierCComponentsTest {

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(new MockApplication() {
            @Override
            protected void init() {
                super.init();
                WicketOats.install(this);
            }
        });
        tester.getSession().setLocale(Locale.US);
    }

    private String html() {
        return tester.getLastResponseAsString();
    }

    private List<TagTester> tags(String attribute, String value) {
        return TagTester.createTagsByAttribute(html(), attribute, value, false);
    }

    private <C extends Component> List<C> find(MarkupContainer root, Class<C> type) {
        List<C> found = new ArrayList<>();
        root.visitChildren(type, (component, visit) -> found.add(type.cast(component)));
        return found;
    }

    // --- CRUD ---

    public static final class Customer implements Serializable {
        String name;
        String city;

        Customer(String name, String city) {
            this.name = name;
            this.city = city;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }
    }

    /** The editor: two Oat fields found by property name through the CompoundPropertyModel. */
    public static final class CustomerFields extends Panel implements IMarkupResourceStreamProvider {
        public CustomerFields(String id, IModel<Customer> model) {
            super(id, model);
            add(new OatTextField<String>("name", Model.of("Name"), null).setRequired(true));
            add(new OatTextField<String>("city", Model.of("City"), null));
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<wicket:panel><div wicket:id='name'></div><div wicket:id='city'></div></wicket:panel>");
        }
    }

    public static class CrudPage extends WebPage implements IMarkupResourceStreamProvider {
        public final List<Customer> customers = new ArrayList<>(List.of(new Customer("ACME", "Vienna"), new Customer("Globex", "Berlin")));
        public final List<String> log = new ArrayList<>();
        public final OatCrud<Customer, String> crud;

        public CrudPage() {
            List<IColumn<Customer, String>> columns = List.of(
                    new LambdaColumn<>(Model.of("Name"), Customer::getName),
                    new LambdaColumn<>(Model.of("City"), Customer::getCity));
            crud = new OatCrud<>("crud", columns, new SortableDataProvider<Customer, String>() {
                @Override
                public Iterator<? extends Customer> iterator(long first, long count) {
                    return customers.stream().skip(first).limit(count).iterator();
                }

                @Override
                public long size() {
                    return customers.size();
                }

                @Override
                public IModel<Customer> model(Customer customer) {
                    return Model.of(customer);
                }
            }, 10)
                    .setEditor(CustomerFields::new)
                    .setNewItem(() -> new Customer(null, null))
                    .setTitle(Customer::getName)
                    .onSave((target, customer) -> {
                        if (!customers.contains(customer)) {
                            customers.add(customer);
                        }
                        log.add("saved " + customer.name + "/" + customer.city);
                    })
                    .onDelete((target, customer) -> {
                        customers.remove(customer);
                        log.add("deleted " + customer.name);
                    });
            add(crud);
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><head></head><body><div wicket:id='crud'></div></body></html>");
        }
    }

    private static final String DIALOG_FORM = "crud:editor:dialog:form";

    /** An entry of a row's actions menu (Edit, Delete); the rows are recreated on every render. */
    private static Component menuItem(WebPage page, String row, int index) {
        List<Component> items = new ArrayList<>();
        page.visitChildren(Component.class, (c, visit) -> {
            if ("items".equals(c.getParent() == null ? null : c.getParent().getId()) && c.getPageRelativePath().contains(row + ":")) {
                items.add(c);
            }
        });
        return items.get(index);
    }

    @Test
    void crudCreatesARecord() {
        CrudPage page = tester.startPage(CrudPage.class);
        assertThat(html()).contains(">New<").contains("Edit").contains("Delete");

        tester.clickLink("crud:table:toolbar:new");
        assertThat(html()).contains(">New entry<").contains(">Save<");

        FormTester form = tester.newFormTester(DIALOG_FORM);
        form.setValue("body:name:container:field", "");
        tester.executeAjaxEvent(DIALOG_FORM + ":confirm", "click");
        assertThat(page.log).isEmpty();
        assertThat(page.customers).hasSize(2);

        form = tester.newFormTester(DIALOG_FORM);
        form.setValue("body:name:container:field", "Initech");
        form.setValue("body:city:container:field", "Munich");
        tester.executeAjaxEvent(DIALOG_FORM + ":confirm", "click");
        assertThat(page.log).containsExactly("saved Initech/Munich");
        assertThat(page.customers).hasSize(3);
        assertThat(html()).contains("Initech");
    }

    @Test
    void crudEditsARecordAndCancelChangesNothing() {
        CrudPage page = tester.startPage(CrudPage.class);
        tester.executeAjaxEvent(menuItem(page, "rows:1", 0), "click"); // Edit on the first row
        assertThat(html()).contains(">Edit ACME<").contains("value=\"Vienna\"");

        // An invalid submit leaves the record untouched
        FormTester form = tester.newFormTester(DIALOG_FORM);
        form.setValue("body:name:container:field", "");
        form.setValue("body:city:container:field", "Graz");
        tester.executeAjaxEvent(DIALOG_FORM + ":confirm", "click");
        assertThat(page.customers.get(0).city).isEqualTo("Vienna");

        form = tester.newFormTester(DIALOG_FORM);
        form.setValue("body:name:container:field", "ACME GmbH");
        form.setValue("body:city:container:field", "Graz");
        tester.executeAjaxEvent(DIALOG_FORM + ":confirm", "click");
        assertThat(page.log).containsExactly("saved ACME GmbH/Graz");
    }

    @Test
    void crudDeletesAfterConfirming() {
        CrudPage page = tester.startPage(CrudPage.class);
        Component delete = menuItem(page, "rows:2", 1); // Delete on the second row
        tester.executeAjaxEvent(delete, "click");
        assertThat(html()).contains("Delete Globex?").contains("This can&#039;t be undone.");
        assertThat(page.customers).hasSize(2);

        tester.executeAjaxEvent("crud:confirm:dialog:form:confirm", "click");
        assertThat(page.log).containsExactly("deleted Globex");
        assertThat(page.customers).hasSize(1);
    }

    // --- Trees ---

    record Folder(String name, List<Folder> children) implements Serializable {
        Folder(String name, Folder... children) {
            this(name, List.of(children));
        }
    }

    static final Folder ROOT = new Folder("Documents", new Folder("Invoices", new Folder("2025"), new Folder("2026")), new Folder("Contracts"));

    static final class FolderProvider implements ITreeProvider<Folder> {
        @Override
        public Iterator<? extends Folder> getRoots() {
            return List.of(ROOT).iterator();
        }

        @Override
        public boolean hasChildren(Folder folder) {
            return !folder.children().isEmpty();
        }

        @Override
        public Iterator<? extends Folder> getChildren(Folder folder) {
            return folder.children().iterator();
        }

        @Override
        public IModel<Folder> model(Folder folder) {
            return Model.of(folder);
        }

        @Override
        public void detach() {
        }
    }

    @Test
    void aTreeExpandsWithAccessibleButtons() {
        List<String> selected = new ArrayList<>();
        OatTree<Folder> tree = new OatTree<>("tree", new FolderProvider()).setLabel(Folder::name)
                .setIcon(f -> f.children().isEmpty() ? "file" : "folder")
                .onSelect((target, folder) -> selected.add(folder.name()));
        tester.startComponentInPage(tree);
        assertThat(html()).contains("class=\"oat-tree\"").contains(">Documents<").doesNotContain(">Invoices<");
        TagTester junction = tags("class", "oat-junction oat-junction-collapsed").get(0);
        assertThat(junction.getName()).isEqualTo("button");
        assertThat(junction.getAttribute("aria-expanded")).isEqualTo("false");
        assertThat(junction.getAttribute("aria-label")).isEqualTo("Expand Documents");

        tester.clickLink(find(tree, OatTreeNode.class).get(0).get("junction"));
        assertThat(html()).contains(">Invoices<").contains(">Contracts<").contains("aria-label=\"Collapse Documents\"");
        // A leaf's button is hidden from screen readers
        assertThat(html()).contains("class=\"oat-junction oat-junction-leaf\"");

        OatTreeNode<?> contracts = find(tree, OatTreeNode.class).stream()
                .filter(n -> ((Folder) n.getModelObject()).name().equals("Contracts")).findFirst().orElseThrow();
        tester.clickLink(contracts.get("content:link"));
        assertThat(selected).containsExactly("Contracts");
        assertThat(html()).contains("aria-current=\"true\"");
    }

    @Test
    void aTableTreeShowsNodesAsRows() {
        List<IColumn<Folder, String>> columns = List.of(new TreeColumn<>(Model.of("Folder")),
                new LambdaColumn<>(Model.of("Items"), (Folder f) -> f.children().size()));
        OatTableTree<Folder, String> tree = new OatTableTree<>("tree", columns, new FolderProvider(), 20).setLabel(Folder::name);
        tester.startComponentInPage(tree);
        assertThat(html()).contains("oat-table-tree").contains(">Documents<").contains(">Folder<").contains(">Items<");
        tester.clickLink(find(tree, OatTreeNode.class).get(0).get("junction"));
        String response = html();
        assertThat(response).contains(">Invoices<").contains(">Contracts<");
        assertThat(response).contains(".oat-junction").contains("focus()");
    }

    // --- Autocomplete fields in dialogs ---

    /** Both autocomplete fields in a dialog, as in a CRUD editor. */
    public static class AutoCompleteDialogPage extends WebPage implements IMarkupResourceStreamProvider {
        public AutoCompleteDialogPage() {
            OatDialog dialog = new OatDialog("dialog", "Edit");
            dialog.setBody(new FieldsPanel(OatDialog.BODY_ID));
            add(dialog);
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><head></head><body><div wicket:id='dialog'></div></body></html>");
        }
    }

    public static final class FieldsPanel extends Panel implements IMarkupResourceStreamProvider {
        public FieldsPanel(String id) {
            super(id);
            add(new dev.jbaby.wicket.oat.components.form.OatAutoCompleteField<String>("one", "One", Model.of((String) null))
                    .setChoices(text -> List.of("Alpha", "Beta")));
            add(new OatMultiSelectField<String>("many", "Many", Model.ofList(new ArrayList<>()))
                    .setChoices(text -> List.of("Alpha", "Beta")));
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<wicket:panel><div wicket:id='one'></div><div wicket:id='many'></div></wicket:panel>");
        }
    }

    @Test
    void autocompleteFieldsLoadTheScriptThatLiftsTheirListAboveADialog() {
        tester.startPage(AutoCompleteDialogPage.class);
        assertThat(html()).contains("wicket-oat.js").contains("wicket-autocomplete");
        assertThat(html().split("wicket-oat\\.js", -1).length - 1).isEqualTo(1);
    }

    // --- Multi-select ---

    @Test
    void aMultiSelectAddsAndRemovesChips() {
        IModel<List<String>> model = Model.ofList(new ArrayList<>(List.of("Jordan Lee")));
        Form<Void> form = new Form<>("form");
        OatMultiSelectField<String> members = new OatMultiSelectField<String>("members", "Members", model)
                .setChoices(text -> List.of("Jordan Lee", "Sam Rivera", "Alex Kim").stream()
                        .filter(n -> n.toLowerCase().contains(text.toLowerCase())).toList());
        members.setRequired(true);
        form.add(members);
        tester.startComponentInPage(form, Markup.of("<form wicket:id='form'><div wicket:id='members'></div></form>"));
        assertThat(tags("class", "oat-chip")).hasSize(1);
        assertThat(html()).contains("aria-label=\"Remove Jordan Lee\"").contains("aria-label=\"Members\"");

        FormComponent<?> search = (FormComponent<?>) members.get("container:field:search");
        tester.getRequest().getPostParameters().setParameterValue(search.getInputName(), "Sam Rivera");
        tester.executeAjaxEvent(search, "change");
        tester.getRequest().getPostParameters().setParameterValue(search.getInputName(), "Jordan Lee"); // already chosen
        tester.executeAjaxEvent(search, "change");
        tester.getRequest().getPostParameters().setParameterValue(search.getInputName(), "Nobody");
        tester.executeAjaxEvent(search, "change");

        FormTester submit = tester.newFormTester("form");
        submit.submit();
        assertThat(model.getObject()).containsExactly("Jordan Lee", "Sam Rivera");

        tester.clickLink("form:members:container:field:chips:chip:0:remove");
        tester.clickLink("form:members:container:field:chips:chip:0:remove");
        submit = tester.newFormTester("form");
        submit.submit();
        assertThat(members.getField().hasErrorMessage()).isTrue(); // required
        assertThat(model.getObject()).containsExactly("Jordan Lee", "Sam Rivera");
    }
}
