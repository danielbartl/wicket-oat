package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.TestcontainersConfiguration;
import dev.jbaby.wicket.oat.components.OatBadge;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class TablePageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testTablePageRenders() {
        tester.startPage(TablePage.class);
        tester.assertRenderedPage(TablePage.class);
        
        // Assert the presence of rows
        tester.assertComponent("rows", org.apache.wicket.markup.html.list.ListView.class);
        
        // Verify we have 3 users (Alice, Bob, Carol)
        tester.assertComponent("rows:0", org.apache.wicket.markup.html.list.ListItem.class);
        tester.assertComponent("rows:1", org.apache.wicket.markup.html.list.ListItem.class);
        tester.assertComponent("rows:2", org.apache.wicket.markup.html.list.ListItem.class);

        // Check content of the first row
        tester.assertLabel("rows:0:name", "Alice Johnson");
        tester.assertLabel("rows:0:email", "alice@example.com");
        tester.assertLabel("rows:0:role", "Admin");
        tester.assertComponent("rows:0:status", OatBadge.class);
        tester.assertLabel("rows:0:status", "Active");
    }
}
