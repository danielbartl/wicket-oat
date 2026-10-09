package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.components.OatDataTable;
import dev.jbaby.wicket.oat.components.table.InvoiceTablePage.Invoice;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationIncrementLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class OatDataTableFeaturesTest {

    private static final String ROWS = "table:table:body:rows";

    private WicketTester tester;
    private InvoiceTablePage page;

    @BeforeEach
    void setUp() {
        tester = new WicketTester();
        tester.getSession().setLocale(Locale.US);
        page = tester.startPage(InvoiceTablePage.class);
    }

    private List<TagTester> tags(String attribute, String value) {
        return TagTester.createTagsByAttribute(tester.getLastResponseAsString(), attribute, value, false);
    }

    /** The components of a type on the page, in tree order (the table recreates its rows on every render). */
    private <C extends Component> List<C> find(Class<C> type) {
        List<C> found = new ArrayList<>();
        page.visitChildren(type, (component, visit) -> found.add(type.cast(component)));
        return found;
    }

    /** The row checkboxes of the current page, in order. */
    private List<Component> rowChecks() {
        return find(OatSelectionColumn.CheckCell.class).stream().filter(c -> !c.header).map(c -> c.get("check")).toList();
    }

    private Component selectAll() {
        return find(OatSelectionColumn.CheckCell.class).stream().filter(c -> c.header).findFirst().orElseThrow().get("check");
    }

    private void nextPage() {
        tester.clickLink(find(PagingNavigationIncrementLink.class).stream().filter(l -> "next".equals(l.getId())).findFirst().orElseThrow());
    }

    private void sortByNumber() {
        tester.clickLink(find(OrderByLink.class).get(0));
    }

    private void check(Component checkBox, boolean checked) {
        FormComponent<?> box = (FormComponent<?>) checkBox;
        if (checked) {
            // A POST parameter, as the browser sends it: executeBehavior replaces the URL's
            tester.getRequest().getPostParameters().setParameterValue(box.getInputName(), "on");
        }
        tester.executeBehavior(box.getBehaviors(AjaxFormComponentUpdatingBehavior.class).get(0));
    }

    // --- Columns ---

    @Test
    void badgeColumnColorsByRow() {
        List<TagTester> badges = tags("class", "badge");
        assertThat(badges).extracting(TagTester::getValue).containsExactly("PAID", "OVERDUE");
        assertThat(badges).extracting(b -> b.getAttribute("data-variant")).containsExactly("success", "danger");
    }

    @Test
    void linkColumnsLinkToAPageOrRunAnAction() {
        TagTester number = TagTester.createTagByAttribute(tester.getLastResponseAsString(), "wicket:id", "link");
        assertThat(number.getName()).isEqualTo("a");
        assertThat(number.getAttribute("href")).contains("id=1");

        tester.clickLink(page.get(ROWS + ":2:cells:3:cell:link"));
        assertThat(page.log).containsExactly("customer Globex");
    }

    @Test
    void numberAndDateColumnsFollowTheLocale() {
        assertThat(tester.getLastResponseAsString()).contains("€1,234.50").contains("$99.00").contains("Oct 9, 2026");

        tester.getSession().setLocale(Locale.GERMANY);
        tester.startPage(page);
        assertThat(tester.getLastResponseAsString()).contains("1.234,50 €").contains("99,00 $").contains("09.10.2026");
    }

    @Test
    void numberColumnsAreRightAligned() {
        TagTester header = tags("scope", "col").get(4);
        assertThat(header.getAttribute("class")).contains("align-right");
        assertThat(tags("class", "align-right")).extracting(TagTester::getValue)
                .anyMatch(cell -> cell.contains("€1,234.50"))
                .anyMatch(cell -> cell.contains("$99.00"));
    }

    @Test
    void numberFormatCanBeSet() {
        OatNumberColumn<Invoice, String> column = new OatNumberColumn<>(Model.of("x"), Invoice::total);
        assertThat(column.setFractionDigits(0, 1)).isSameAs(column);
        assertThatIllegalArgumentException().isThrownBy(() -> column.setFractionDigits(2, 1));
        assertThatIllegalArgumentException().isThrownBy(() -> new OatDateColumn<Invoice, String>(Model.of("x"), Invoice::due).setPattern("yyyy-MM-dd'"));
    }

    @Test
    void booleanColumnShowsAReadOnlyCheckbox() {
        List<TagTester> boxes = tags("class", "oat-readonly");
        assertThat(boxes).hasSize(2);
        assertThat(boxes.get(0).getAttribute("checked")).isEqualTo("checked");
        assertThat(boxes.get(1).getAttribute("checked")).isNull();
        assertThat(boxes).allMatch(b -> b.getAttribute("disabled") != null && "Exported".equals(b.getAttribute("aria-label")));
    }

    @Test
    void nullValuesLeaveTheCellEmpty() {
        nextPage();
        String html = tester.getLastResponseAsString();
        assertThat(html).contains("Initech").doesNotContain("¥").doesNotContain("oat-readonly");
    }

    // --- Sorting ---

    @Test
    void sortedHeadersTellScreenReadersTheOrder() {
        assertThat(tags("aria-sort", "ascending")).isEmpty();
        sortByNumber();
        assertThat(tags("aria-sort", "ascending")).hasSize(1);
        assertThat(tags("aria-sort", "ascending").get(0).getAttribute("class")).contains("wicket_orderUp");
        sortByNumber();
        assertThat(tags("aria-sort", "descending")).hasSize(1);
    }

    // --- Row actions ---

    @Test
    void actionsMenuShowsTheRowsActions() {
        List<TagTester> triggers = tags("aria-label", "Actions");
        assertThat(triggers).hasSize(2).allMatch(t -> "ghost small icon".equals(t.getAttribute("class")));

        List<TagTester> items = tags("role", "menuitem");
        // Row 1 is paid, so it has no "Mark as paid"
        assertThat(items).extracting(TagTester::getValue).containsExactly(
                "<span wicket:id=\"label\">Edit</span>", "<span wicket:id=\"label\">Delete</span>",
                "<span wicket:id=\"label\">Edit</span>", "<span wicket:id=\"label\">Mark as paid</span>", "<span wicket:id=\"label\">Delete</span>");
        assertThat(items.get(1).getAttribute("data-variant")).isEqualTo("danger");

        tester.executeAjaxEvent(page.get(ROWS + ":2:cells:8:cell:menu:items:1"), "click");
        assertThat(page.log).containsExactly("paid INV-2");
        assertThat(tester.getLastResponseAsString()).contains("hidePopover()");
    }

    // --- Selection and bulk actions ---

    @Test
    void selectingRowsShowsTheBulkBar() {
        tester.assertInvisible("table:bulkActions");
        assertThat(tags("aria-label", "Select row")).hasSize(2);
        assertThat(tags("aria-label", "Select all rows on this page")).hasSize(1);

        check(rowChecks().get(0), true);
        assertThat(page.selected.getObject()).extracting(Invoice::number).containsExactly("INV-1");
        tester.assertComponentOnAjaxResponse(page.table.get("bulkActions"));
        tester.assertLabel("table:bulkActions:count", "1 selected");

        check(rowChecks().get(0), false);
        assertThat(page.selected.getObject()).isEmpty();
    }

    @Test
    void selectAllSelectsTheCurrentPageAndBulkActionsGetTheRows() {
        check(selectAll(), true);
        assertThat(page.selected.getObject()).extracting(Invoice::number).containsExactlyInAnyOrder("INV-1", "INV-2");

        // Selection survives paging
        nextPage();
        check(rowChecks().get(0), true);
        assertThat(page.selected.getObject()).hasSize(3);

        tester.executeAjaxEvent(page.get("table:bulkActions:actions:0:action"), "click");
        assertThat(page.log).containsExactly("export [INV-1, INV-2, INV-3]");
        assertThat(page.selected.getObject()).isEmpty();
        tester.assertComponentOnAjaxResponse(page.table);
    }

    @Test
    void clearSelectionEmptiesIt() {
        check(rowChecks().get(0), true);
        tester.executeAjaxEvent(page.get("table:bulkActions:clear"), "click");
        assertThat(page.selected.getObject()).isEmpty();
    }

    @Test
    void bulkActionsNeedASelectionColumn() {
        List<IColumn<Invoice, String>> columns = List.of(new OatDateColumn<>(Model.of("Due"), Invoice::due));
        OatDataTable<Invoice, String> table = new OatDataTable<>("t", columns, new EmptyProvider(), 10);
        assertThatIllegalStateException().isThrownBy(() -> table.addBulkAction("Export", (target, rows) -> { }));
    }

    // --- Toolbar ---

    @Test
    void toolbarIsShownAboveTheTable() {
        assertThat(tester.getTagByWicketId("toolbar").getValue()).isEqualTo("Toolbar");
        assertThatIllegalArgumentException().isThrownBy(() -> page.table.setToolbar(id -> new org.apache.wicket.markup.html.basic.Label("x")));
        assertThat(page.table.getOutputMarkupId()).isTrue();
    }

    private static class EmptyProvider extends SortableDataProvider<Invoice, String> {
        @Override
        public Iterator<? extends Invoice> iterator(long first, long count) {
            return List.<Invoice>of().iterator();
        }

        @Override
        public long size() {
            return 0;
        }

        @Override
        public IModel<Invoice> model(Invoice object) {
            return Model.of(object);
        }
    }
}
