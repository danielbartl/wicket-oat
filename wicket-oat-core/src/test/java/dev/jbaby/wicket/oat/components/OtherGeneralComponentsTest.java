package dev.jbaby.wicket.oat.components;

import dev.jbaby.wicket.oat.OatSession;
import dev.jbaby.wicket.oat.OatTheme;
import org.apache.wicket.Session;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

class OtherGeneralComponentsTest {
    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(new MockApplication() {
            @Override
            public Session newSession(Request request, Response response) {
                return new OatSession(request);
            }
        });
    }

    public static class TestData implements Serializable {
        public String name;
        public TestData(String name) { this.name = name; }
        public String getName() { return name; }
    }

    @Test
    void testOatDataTable() {
        List<TestData> list = Arrays.asList(new TestData("A"), new TestData("B"));
        SortableDataProvider<TestData, String> provider = new SortableDataProvider<TestData, String>() {
            @Override
            public Iterator<? extends TestData> iterator(long first, long count) {
                return list.iterator();
            }
            @Override
            public long size() { return list.size(); }
            @Override
            public IModel<TestData> model(TestData object) { return Model.of(object); }
        };
        
        List<IColumn<TestData, String>> columns = new ArrayList<>();
        columns.add(new PropertyColumn<>(Model.of("Name"), "name", "name"));
        
        OatDataTable<TestData, String> table = new OatDataTable<>("table", columns, provider, 10);
        tester.startComponentInPage(table);
        tester.assertComponent("table:table", org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable.class);
    }

    @Test
    void testOatAccordion() {
        List<String> data = Arrays.asList("1", "2");
        OatAccordion<String> accordion = new OatAccordion<String>("accordion", Model.ofList(data)) {
            @Override
            protected void populateItem(ListItem<String> item) {
                item.add(new Label("title", "Title " + item.getModelObject()));
                item.add(new Label("content", "Content " + item.getModelObject()));
            }
        };
        // Use full markup since startComponentInPage can be tricky with abstract components nested
        tester.startComponentInPage(accordion, Markup.of("<details wicket:id=\"accordion\"><summary wicket:id=\"title\"></summary><div wicket:id=\"content\"></div></details>"));
        tester.assertLabel("accordion:0:title", "Title " + data.get(0));
    }

    @Test
    void testOatEmptyState() {
        OatEmptyState empty = new OatEmptyState("empty", Model.of("No Data"), Model.of("Please add some."));
        tester.startComponentInPage(empty);
        tester.assertLabel("empty:title", "No Data");
        tester.assertLabel("empty:message", "Please add some.");
    }

    public static class MockAppLayout extends OatAppLayout {
        @Override
        protected IModel<List<MenuItem>> sidebarMenuItemsModel() {
            return Model.ofList(List.of(new MenuItem("Home", MockAppLayout.class)));
        }
    }

    @Test
    void testOatAppLayout() {
        tester.startPage(new MockAppLayout());
        tester.assertLabel("appTitle", "Wicket Oat Application");
    }
}
