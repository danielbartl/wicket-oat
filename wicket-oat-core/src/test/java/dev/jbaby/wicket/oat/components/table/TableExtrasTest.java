package dev.jbaby.wicket.oat.components.table;

import dev.jbaby.wicket.oat.WicketOats;
import dev.jbaby.wicket.oat.components.OatDataTable;
import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.LambdaColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.validation.validator.RangeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class TableExtrasTest {

    public static final class Line implements Serializable {
        final int id;
        final String product;
        Integer quantity;
        final BigDecimal price;
        final LocalDate added;

        Line(int id, String product, Integer quantity, BigDecimal price, LocalDate added) {
            this.id = id;
            this.product = product;
            this.quantity = quantity;
            this.price = price;
            this.added = added;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Line line && line.id == id;
        }

        @Override
        public int hashCode() {
            return id;
        }
    }

    /** Order lines with details, an editable quantity, a column chooser and a CSV export. */
    public static class LinesPage extends WebPage implements IMarkupResourceStreamProvider {
        public final List<Line> lines = new ArrayList<>(List.of(
                new Line(1, "Widget, large", 2, new BigDecimal("19.90"), LocalDate.of(2026, 10, 1)),
                new Line(2, "=HYPERLINK(\"x\")", 1, new BigDecimal("-5"), LocalDate.of(2026, 10, 2)),
                new Line(3, "Gadget", null, new BigDecimal("1234.5"), null)));
        public final List<String> saved = new ArrayList<>();
        public final OatDataTable<Line, String> table;

        public LinesPage() {
            List<IColumn<Line, String>> columns = new ArrayList<>();
            columns.add(new OatRowDetailsColumn<>((id, line) -> new Label(id, () -> "Details of " + line.getObject().product)));
            columns.add(new LambdaColumn<>(Model.of("Product"), "product", (Line line) -> line.product));
            columns.add(new OatEditableColumn<Line, String, Integer>(Model.of("Quantity"), line -> line.quantity, (line, q) -> line.quantity = q)
                    .setType(Integer.class).setRequired(true).addValidator(RangeValidator.minimum(1))
                    .onSave((target, line) -> saved.add(line.product + "=" + line.quantity)));
            columns.add(new OatNumberColumn<>(Model.of("Price"), line -> line.price));
            columns.add(new OatDateColumn<>(Model.of("Added"), line -> line.added));
            table = new OatDataTable<>("table", columns, new SortableDataProvider<>() {
                {
                    setSort("product", org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder.ASCENDING);
                }

                @Override
                public Iterator<? extends Line> iterator(long first, long count) {
                    Comparator<Line> order = Comparator.comparing(line -> line.product);
                    if (!getSort().isAscending()) {
                        order = order.reversed();
                    }
                    return lines.stream().sorted(order).skip(first).limit(count).iterator();
                }

                @Override
                public long size() {
                    return lines.size();
                }

                @Override
                public IModel<Line> model(Line line) {
                    return Model.of(line);
                }
            }, 10);
            table.setColumnChooser(true).setCsvExport("lines.csv");
            add(table);
        }

        @Override
        public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass) {
            return new StringResourceStream("<html><head></head><body><div wicket:id='table'></div></body></html>");
        }
    }

    private WicketTester tester;
    private LinesPage page;

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
        page = tester.startPage(LinesPage.class);
    }

    private String html() {
        return tester.getLastResponseAsString();
    }

    private <C extends Component> List<C> find(Class<C> type) {
        List<C> found = new ArrayList<>();
        page.visitChildren(type, (component, visit) -> found.add(type.cast(component)));
        return found;
    }

    private List<TagTester> tags(String attribute, String value) {
        return TagTester.createTagsByAttribute(html(), attribute, value, false);
    }

    @Test
    void eachRowIsATbodyWithAHiddenDetailsRow() {
        assertThat(tags("class", "oat-row")).hasSize(3);
        assertThat(html()).doesNotContain("Details of");
        assertThat(tags("aria-expanded", "false")).hasSize(3);
    }

    @Test
    void theToggleExpandsARowIntoDetails() {
        OatRowDetailsColumn<?, ?>.ToggleCell first = find(OatRowDetailsColumn.ToggleCell.class).get(0);
        tester.clickLink(first.get("toggle"));
        String response = html();
        assertThat(response).contains("Details of =HYPERLINK").contains("aria-expanded=\"true\"").contains("colspan=\"5\"");

        // Stays open on a full render, then closes again
        tester.startPage(page);
        assertThat(html()).contains("Details of =HYPERLINK");
        tester.clickLink(find(OatRowDetailsColumn.ToggleCell.class).get(0).get("toggle"));
        assertThat(html()).doesNotContain("Details of");
    }

    @Test
    void anEditableCellSavesAValidValue() {
        List<TagTester> buttons = tags("class", "ghost small oat-editable");
        assertThat(buttons).hasSize(3);
        assertThat(buttons.get(0).getAttribute("aria-label")).isEqualTo("Edit Quantity: 1");
        assertThat(buttons.get(1).getValue()).contains("—"); // Gadget has no quantity

        OatEditableColumn<?, ?, ?>.EditableCell cell = find(OatEditableColumn.EditableCell.class).get(1); // Gadget
        tester.clickLink(cell.get("content:edit"));
        assertThat(html()).contains("data-oat-escape=\"cancel\"").contains("aria-label=\"Quantity\"");

        FormTester form = tester.newFormTester(cell.get("content:form").getPageRelativePath());
        form.setValue("input", "0");
        tester.executeAjaxEvent(cell.get("content:form:save"), "click");
        assertThat(html()).contains("oat-editing-error").contains("aria-invalid=\"true\"");
        assertThat(page.saved).isEmpty();
        // Shown in the cell, so a feedback panel or toast leaves it out
        Component input = cell.get("content:form:input");
        assertThat(new dev.jbaby.wicket.oat.components.form.NotShownInlineFilter()
                .accept(input.getFeedbackMessages().first(org.apache.wicket.feedback.FeedbackMessage.ERROR))).isFalse();

        form = tester.newFormTester(cell.get("content:form").getPageRelativePath());
        form.setValue("input", "4");
        tester.executeAjaxEvent(cell.get("content:form:save"), "click");
        assertThat(page.saved).containsExactly("Gadget=4");
        assertThat(html()).contains("aria-label=\"Edit Quantity: 4\"");
    }

    @Test
    void cancelLeavesTheValue() {
        OatEditableColumn<?, ?, ?>.EditableCell cell = find(OatEditableColumn.EditableCell.class).get(0);
        tester.clickLink(cell.get("content:edit"));
        tester.clickLink(cell.get("content:form:cancel"));
        assertThat(html()).contains("Edit Quantity: 1");
        assertThat(page.saved).isEmpty();
    }

    @Test
    void theChooserHidesAColumn() {
        assertThat(tags("wicket:id", "name")).extracting(TagTester::getValue)
                .containsExactly("Product", "Quantity", "Price", "Added");
        @SuppressWarnings("unchecked")
        FormComponent<Boolean> price = (FormComponent<Boolean>) find(FormComponent.class).stream()
                .filter(c -> "check".equals(c.getId())).toList().get(2);
        tester.getRequest().getPostParameters().setParameterValue(price.getInputName(), "");
        tester.executeAjaxEvent(price, "click");
        assertThat(page.table.isColumnVisible(3)).isFalse();
        tester.startPage(page);
        // The header and every cell of the price column
        assertThat(html().split("oat-column-hidden", -1).length - 1).isEqualTo(4);
        // A details row spans the visible columns only
        tester.clickLink(find(OatRowDetailsColumn.ToggleCell.class).get(0).get("toggle"));
        assertThat(html()).contains("colspan=\"4\"");
    }

    @Test
    void theCsvHasEveryRowSortedAndSafe() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OatCsvExport.write(page.table.getTable().getDataProvider(), page.table.getTable().getColumns(), Locale.US, out);
        String csv = out.toString(StandardCharsets.UTF_8);
        assertThat(csv).isEqualTo("﻿Product,Quantity,Price,Added\r\n"
                + "'=HYPERLINK(\"\"x\"\"),1,-5,2026-10-02\r\n".replace("'=HYPERLINK(\"\"x\"\")", "\"'=HYPERLINK(\"\"x\"\")\"")
                + "Gadget,,1234.5,\r\n"
                + "\"Widget, large\",2,19.90,2026-10-01\r\n");
    }

    @Test
    void theExportButtonDownloadsTheFile() {
        tester.clickLink(find(Link.class).stream().filter(l -> "export".equals(l.getId())).findFirst().orElseThrow());
        assertThat(tester.getLastResponse().getHeader("Content-Disposition")).contains("attachment").contains("lines.csv");
        assertThat(tester.getLastResponse().getContentType()).startsWith("text/csv");
        assertThat(tester.getLastResponse().getDocument()).contains("Gadget,,1234.5,");
    }

    @Test
    void sortingStillWorks() {
        tester.clickLink(find(OrderByLink.class).get(0)); // sorted ascending at first, now descending
        assertThat(tags("class", "oat-row").get(0).getValue()).contains("Widget");
    }
}
