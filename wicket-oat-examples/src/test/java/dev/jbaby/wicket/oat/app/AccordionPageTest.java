package dev.jbaby.wicket.oat.app;

import dev.jbaby.wicket.oat.TestcontainersConfiguration;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.apache.wicket.protocol.http.WebApplication;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class AccordionPageTest {

    @Autowired
    private WebApplication wicketApp;

    private WicketTester tester;

    @BeforeEach
    void setUp() {
        tester = new WicketTester(wicketApp);
    }

    @Test
    void testAccordionPageRenders() {
        tester.startPage(AccordionPage.class);
        tester.assertRenderedPage(AccordionPage.class);
        
        // Assert the presence of the accordion component
        tester.assertComponent("accordion", dev.jbaby.wicket.oat.components.OatAccordion.class);
        
        // Assert that we have 3 items (based on the sample data in AccordionPage)
        tester.assertComponent("accordion:0", org.apache.wicket.markup.html.list.ListItem.class);
        tester.assertComponent("accordion:1", org.apache.wicket.markup.html.list.ListItem.class);
        tester.assertComponent("accordion:2", org.apache.wicket.markup.html.list.ListItem.class);

        // Check the title of the first item
        tester.assertLabel("accordion:0:title", "Accordion Item 1");
    }
}
