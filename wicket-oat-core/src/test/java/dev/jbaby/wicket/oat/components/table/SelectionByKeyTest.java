package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.components.OatDataTable;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationIncrementLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.util.SetModel;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Selecting and expanding rows by key, for entities loaded anew on every request. */
class SelectionByKeyTest {

    /** Like a JPA entity: no equals, and a new instance every time it is loaded. */
    public static final class Account implements Serializable {
        final long id;
        final String name;

        Account(long id, String name) {
            this.id = id;
            this.name = name;
        }

        long getId() {
            return id;
        }
    }

    public static class AccountsPage extends WebPage implements IMarkupResourceStreamProvider {

        /** The "database": names by id. */
        public final Map<Long, String> database = new LinkedHashMap<>();
        public final IModel<Set<Long>> selectedIds = new SetModel<>(new HashSet<>());
        public final IModel<Set<Long>> expandedIds = new SetModel<>(new HashSet<>());
        public final List<String> log = new ArrayList<>();
        /** The keys the selection column loaded rows for. */
        public final List<Long> loaded = new ArrayList<>();
        /** Shows only the accounts whose name contains it. */
        public String filter = "";
        public final OatDataTable<Account, String> table;

        public AccountsPage() {
            this(false);
        }

        /** @param withLoader whether the selection column loads the selected rows by key */
        public AccountsPage(boolean withLoader) {
            database.put(1L, "ACME");
            database.put(2L, "Globex");
            database.put(3L, "Initech");
            List<IColumn<Account, String>> columns = new ArrayList<>();
            columns.add(withLoader
                    ? new OatSelectionColumn<>(selectedIds, Account::getId, ids -> {
                        loaded.addAll(ids);
                        return ids.stream().sorted().map(id -> new Account(id, database.get(id))).toList();
                    })
                    : new OatSelectionColumn<>(selectedIds, Account::getId));
            columns.add(new OatRowDetailsColumn<>((id, account) -> new Label(id, () -> "Details of " + account.getObject().name),
                    expandedIds, Account::getId));
            columns.add(new LambdaColumn<>(Model.of("Name"), (Account account) -> account.name));
            table = new OatDataTable<>("table", columns, new SortableDataProvider<Account, String>() {
                @Override
                public Iterator<? extends Account> iterator(long first, long count) {
                    return database.entrySet().stream().filter(e -> e.getValue().contains(filter)).skip(first).limit(count)
                            .map(e -> new Account(e.getKey(), e.getValue())).iterator();
                }

                @Override
                public long size() {
                    return database.values().stream().filter(name -> name.contains(filter)).count();
                }

                @Override
                public IModel<Account> model(Account object) {
                    return Model.of(object);
                }
            }, 2);
            table.addBulkAction("Archive", (target, rows) -> log.add("archive " + rows.stream().map(a -> a.name).toList()));
            add(table);
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><body><div wicket:id='table'></div></body></html>");
        }
    }

    private WicketTester tester;
    private AccountsPage page;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        page = tester.startPage(AccountsPage.class);
    }

    @Test
    void theSelectionKeepsOnlyKeysAndBulkActionsGetCurrentRows() {
        check(rowChecks().get(0));
        assertThat(page.selectedIds.getObject()).containsExactly(1L);
        // Re-rendered with new instances, the row is still checked
        tester.startPage(page);
        assertThat(isChecked(rowChecks().get(0))).isTrue();

        check(selectAll());
        nextPage();
        check(rowChecks().get(0));
        assertThat(page.selectedIds.getObject()).containsExactlyInAnyOrder(1L, 2L, 3L);

        // Looked up when the action runs: in the provider's order, with current data
        page.database.put(2L, "Globex Corp");
        tester.executeAjaxEvent(page.get("table:bulkActions:actions:0:action"), "click");
        assertThat(page.log).containsExactly("archive [ACME, Globex Corp, Initech]");
    }

    @Test
    void withALoaderBulkActionsAlsoGetSelectedRowsAFilterHides() {
        tester.getSession().setLocale(Locale.ENGLISH);
        page = tester.startPage(new AccountsPage(true));
        check(selectAll()); // ACME and Globex, on the first page

        page.filter = "AC"; // hides Globex
        tester.startPage(page);
        tester.executeAjaxEvent(page.get("table:bulkActions:actions:0:action"), "click");

        assertThat(page.log).containsExactly("archive [ACME, Globex]");
        assertThat(page.loaded).containsExactlyInAnyOrder(1L, 2L);
        tester.assertLabel("table:bulkActions:count", "2 selected");
    }

    @Test
    void withoutALoaderTheRowsAreLookedUpAmongThoseTheProviderReturns() {
        check(selectAll());

        page.filter = "AC";
        tester.startPage(page);
        tester.executeAjaxEvent(page.get("table:bulkActions:actions:0:action"), "click");

        assertThat(page.log).containsExactly("archive [ACME]");
    }

    @Test
    void expandedRowsAreRememberedByKey() {
        tester.clickLink(find(OatRowDetailsColumn.ToggleCell.class).get(0).get("toggle"));
        assertThat(page.expandedIds.getObject()).containsExactly(1L);

        tester.startPage(page); // new instances
        assertThat(tester.getLastResponseAsString()).contains("Details of ACME").doesNotContain("Details of Globex");
    }

    private <C extends Component> List<C> find(Class<C> type) {
        List<C> found = new ArrayList<>();
        page.visitChildren(type, (component, visit) -> found.add(type.cast(component)));
        return found;
    }

    private List<Component> rowChecks() {
        return find(OatSelectionColumn.CheckCell.class).stream().filter(c -> !c.header).map(c -> c.get("check")).toList();
    }

    private Component selectAll() {
        return find(OatSelectionColumn.CheckCell.class).stream().filter(c -> c.header).findFirst().orElseThrow().get("check");
    }

    private boolean isChecked(Component checkBox) {
        return Boolean.TRUE.equals(checkBox.getDefaultModelObject());
    }

    private void check(Component checkBox) {
        FormComponent<?> box = (FormComponent<?>) checkBox;
        // A POST parameter, as the browser sends it: executeBehavior replaces the URL's
        tester.getRequest().getPostParameters().setParameterValue(box.getInputName(), "on");
        tester.executeBehavior(box.getBehaviors(AjaxFormComponentUpdatingBehavior.class).get(0));
    }

    private void nextPage() {
        tester.clickLink(find(PagingNavigationIncrementLink.class).stream().filter(l -> "next".equals(l.getId())).findFirst().orElseThrow());
    }
}
